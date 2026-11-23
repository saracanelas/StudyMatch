package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.StudentCurricularUnitRepository;
import com.studymatch.studymatch.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Enrolls students in curricular units and registers their grades,
 * keeping each student's grade average up to date.
 */
@Service
public class StudentCurricularUnitService {

    private final StudentCurricularUnitRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final GradeAverageService gradeAverageService;

    /**
     * Receives the repositories and the service that recalculates grade averages.
     */
    public StudentCurricularUnitService(StudentCurricularUnitRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CurricularUnitRepository curricularUnitRepository,
                             GradeAverageService gradeAverageService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.curricularUnitRepository = curricularUnitRepository;
        this.gradeAverageService = gradeAverageService;
    }

    /**
     * Returns every enrollment.
     */
    @Transactional(readOnly = true)
    public List<StudentCurricularUnit> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * Returns the enrollments (with grades) of a student, or 404 if the student does not exist.
     */
    @Transactional(readOnly = true)
    public List<StudentCurricularUnit> getEnrollmentsOfStudent(Long idStudent) {
        findStudentOrThrow(idStudent);
        return enrollmentRepository.findByStudent_IdStudent(idStudent);
    }

    /**
     * Returns the students enrolled in a curricular unit, or 404 if the unit does not exist.
     */
    @Transactional(readOnly = true)
    public List<StudentCurricularUnit> getEnrollmentsOfUnit(Long idUnit) {
        findUnitOrThrow(idUnit);
        return enrollmentRepository.findByCurricularUnit_IdUnit(idUnit);
    }

    /**
     * Enrolls a student in a curricular unit without a grade.
     * Fails with 400 if an id is missing, 404 if the student or unit does not exist
     * and 409 if the student is already enrolled in that unit.
     */
    @Transactional
    public StudentCurricularUnit enroll(Long idStudent, Long idUnit) {
        if (idStudent == null || idUnit == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique o estudante e a UC");
        }
        Student student = findStudentOrThrow(idStudent);
        CurricularUnit unit = findUnitOrThrow(idUnit);
        if (enrollmentRepository.existsByStudent_IdStudentAndCurricularUnit_IdUnit(idStudent, idUnit)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    student.getNameStudent() + " já está inscrito(a) em " + unit.getNameUnit());
        }
        return enrollmentRepository.save(new StudentCurricularUnit(student, unit));
    }

    /**
     * Registers or changes the grade of an enrollment (null clears it) and updates the student's average.
     * Fails with 400 if the grade is not between 0 and 20 and 404 if the enrollment does not exist.
     */
    @Transactional
    public StudentCurricularUnit setGrade(Long idEnrollment, Double grade) {
        if (grade != null && (grade.isNaN() || !StudentCurricularUnit.isValidGrade(grade))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota tem de estar entre 0 e 20");
        }
        StudentCurricularUnit enrollment = findEnrollmentOrThrow(idEnrollment);
        enrollment.setCuGrade(grade);
        enrollmentRepository.save(enrollment);
        gradeAverageService.recalculate(enrollment.getStudent());
        return enrollment;
    }

    /**
     * Removes an enrollment and updates the student's average; 404 if it does not exist.
     */
    @Transactional
    public void unenroll(Long idEnrollment) {
        StudentCurricularUnit enrollment = findEnrollmentOrThrow(idEnrollment);
        enrollmentRepository.delete(enrollment);
        enrollmentRepository.flush();
        gradeAverageService.recalculate(enrollment.getStudent());
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estudante " + id + " não encontrado"));
    }

    private CurricularUnit findUnitOrThrow(Long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + id + " não encontrada"));
    }

    private StudentCurricularUnit findEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inscrição " + id + " não encontrada"));
    }
}
