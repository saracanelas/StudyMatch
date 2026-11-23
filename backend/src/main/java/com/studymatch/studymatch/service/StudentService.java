package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.StudentRequest;
import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.repository.CourseRepository;
import com.studymatch.studymatch.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    /**
     * Receives the repositories used to read and save students and to look up their course.
     */
    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Returns every student in the database.
     */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Returns the student with the given id, or 404 if it does not exist.
     */
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return findStudentOrThrow(id);
    }

    /**
     * Saves a new student, refusing it if the student number or e-mail is already in use
     * or if the indicated course does not exist. The grade average starts empty.
     */
    @Transactional
    public Student createStudent(StudentRequest newStudent) {
        Student student = new Student();
        copyFields(newStudent, student, null);
        return studentRepository.save(student);
    }

    /**
     * Changes the data of an existing student, keeping student numbers and e-mails unique.
     * The grade average is not changed here: it only depends on the grades.
     */
    @Transactional
    public Student updateStudent(Long id, StudentRequest updatedStudent) {
        Student student = findStudentOrThrow(id);
        copyFields(updatedStudent, student, id);
        return student; // saved automatically when the transaction ends
    }

    /**
     * Copies the editable fields from `source` to `target` after checking that the number and e-mail
     * are not used by another student (`ownId` is the id of the student being edited, or null).
     */
    private void copyFields(StudentRequest source, Student target, Long ownId) {
        String number = source.numberStudent().trim();
        String email = source.emailStudent().trim().toLowerCase();

        Student sameNumber = studentRepository.findByNumberStudent(number);
        if (sameNumber != null && !sameNumber.getIdStudent().equals(ownId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um estudante com o número " + number);
        }
        Student sameEmail = studentRepository.findByEmailStudent(email);
        if (sameEmail != null && !sameEmail.getIdStudent().equals(ownId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um estudante com o email " + email);
        }

        target.setNumberStudent(number);
        target.setNameStudent(source.nameStudent().trim());
        target.setEmailStudent(email);
        target.setBirthday(source.birthday());
        target.setEnrollmentYear(source.enrollmentYear());
        String classStudent = source.classStudent();
        target.setClassStudent(classStudent == null || classStudent.isBlank() ? null : classStudent.trim());
        target.setCourse(findCourse(source.idCourse()));
    }

    /**
     * Returns the stored course with the id sent by the client, or null if none was sent.
     */
    private Course findCourse(Long idCourse) {
        if (idCourse == null) {
            return null;
        }
        return courseRepository.findById(idCourse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Curso " + idCourse + " não encontrado"));
    }

    /**
     * Looks up a student by id and fails with 404 when it is missing.
     */
    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estudante " + id + " não encontrado"));
    }
}
