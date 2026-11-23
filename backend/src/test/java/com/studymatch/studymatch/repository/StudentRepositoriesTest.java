package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StudentRepositoriesTest {

    @Autowired private StudentRepository studentRepository;
    @Autowired private StudentCurricularUnitRepository enrollmentRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CurricularUnitRepository unitRepository;
    @Autowired private TestEntityManager em;

    private Student student(String number, String email) {
        return new Student(null, number, "Student " + number, email, 2024);
    }

    @Test
    @DisplayName("A student and all its fields are persisted and loaded back")
    void studentPersists() {
        Course course = courseRepository.save(new Course(null, "LEI", "Computer Engineering"));
        Student s = student("2024001", "ana@studymatch.pt");
        s.setBirthday(LocalDate.of(2004, 5, 1));
        s.setCourse(course);
        s.setGradeAverage(14.25);
        Long id = studentRepository.save(s).getIdStudent();
        em.flush();
        em.clear();

        Student loaded = studentRepository.findById(id).orElseThrow();
        assertEquals("2024001", loaded.getNumberStudent());
        assertEquals("ana@studymatch.pt", loaded.getEmailStudent());
        assertEquals(LocalDate.of(2004, 5, 1), loaded.getBirthday());
        assertEquals(2024, loaded.getEnrollmentYear());
        assertEquals("LEI", loaded.getCourse().getCodeCourse());
        assertEquals(14.25, loaded.getGradeAverage());
    }

    @Test
    @DisplayName("findByNumberStudent and findByEmailStudent find exact matches and return null otherwise")
    void findByNumberAndEmail() {
        studentRepository.save(student("2024001", "ana@studymatch.pt"));

        assertNotNull(studentRepository.findByNumberStudent("2024001"));
        assertNull(studentRepository.findByNumberStudent("999"));
        assertNotNull(studentRepository.findByEmailStudent("ana@studymatch.pt"));
        assertNull(studentRepository.findByEmailStudent("other@studymatch.pt"));
    }

    @Test
    @DisplayName("Name search, course and enrollment year queries work")
    void otherQueries() {
        Course lei = courseRepository.save(new Course(null, "LEI", "Computer Engineering"));
        Student ana = student("1", "ana@x.pt");
        ana.setNameStudent("Ana Costa");
        ana.setCourse(lei);
        studentRepository.save(ana);
        Student rui = student("2", "rui@x.pt");
        rui.setNameStudent("Rui Silva");
        rui.setEnrollmentYear(2023);
        studentRepository.save(rui);

        assertEquals(1, studentRepository.findByNameStudentContainingIgnoreCase("COSTA").size());
        assertEquals(List.of("1"), studentRepository.findByCourse_IdCourse(lei.getIdCourse()).stream().map(Student::getNumberStudent).toList());
        assertEquals(1, studentRepository.findByEnrollmentYear(2023).size());
    }

    @Test
    @DisplayName("Two students cannot share the same number or e-mail")
    void numberAndEmailAreUnique() {
        studentRepository.saveAndFlush(student("1", "a@x.pt"));

        assertThrows(DataIntegrityViolationException.class,
                () -> studentRepository.saveAndFlush(student("1", "b@x.pt")));
        em.clear();
        assertThrows(DataIntegrityViolationException.class,
                () -> studentRepository.saveAndFlush(student("2", "a@x.pt")));
    }

    @Test
    @DisplayName("An enrollment with its grade is persisted and found by student and by unit")
    void enrollmentPersists() {
        Student s = studentRepository.save(student("1", "a@x.pt"));
        CurricularUnit unit = unitRepository.save(new CurricularUnit(null, "SQ", "Software Quality"));
        StudentCurricularUnit enrollment = new StudentCurricularUnit(s, unit);
        enrollment.setCuGrade(16.5);
        enrollmentRepository.save(enrollment);
        em.flush();
        em.clear();

        List<StudentCurricularUnit> ofStudent = enrollmentRepository.findByStudent_IdStudent(s.getIdStudent());
        List<StudentCurricularUnit> ofUnit = enrollmentRepository.findByCurricularUnit_IdUnit(unit.getIdUnit());
        assertEquals(1, ofStudent.size());
        assertEquals(16.5, ofStudent.get(0).getCuGrade());
        assertEquals("SQ", ofStudent.get(0).getCurricularUnit().getCodeUnit());
        assertEquals(1, ofUnit.size());
        assertEquals("1", ofUnit.get(0).getStudent().getNumberStudent());
        assertTrue(enrollmentRepository.existsByStudent_IdStudentAndCurricularUnit_IdUnit(s.getIdStudent(), unit.getIdUnit()));
        assertFalse(enrollmentRepository.existsByStudent_IdStudentAndCurricularUnit_IdUnit(s.getIdStudent(), 999L));
    }

    @Test
    @DisplayName("A student cannot be enrolled twice in the same unit")
    void enrollmentIsUnique() {
        Student s = studentRepository.save(student("1", "a@x.pt"));
        CurricularUnit unit = unitRepository.save(new CurricularUnit(null, "SQ", "Software Quality"));
        enrollmentRepository.saveAndFlush(new StudentCurricularUnit(s, unit));

        assertThrows(DataIntegrityViolationException.class,
                () -> enrollmentRepository.saveAndFlush(new StudentCurricularUnit(s, unit)));
    }

    @Test
    @DisplayName("The ECTS of a unit are stored")
    void unitEctsPersist() {
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");
        unit.setEcts(7);
        Long id = unitRepository.save(unit).getIdUnit();
        em.flush();
        em.clear();

        assertEquals(7, unitRepository.findById(id).orElseThrow().getEcts());
    }
}
