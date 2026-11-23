package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.repository.StudentCurricularUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Keeps the students' grade average up to date.
 *
 * The average is weighted by the ECTS of each curricular unit:
 * sum(grade x ECTS) / sum(ECTS), counting only the units that already have a grade,
 * rounded to two decimal places. A student without grades has no average (null).
 */
@Service
public class GradeAverageService {

    private final StudentCurricularUnitRepository enrollmentRepository;

    /**
     * Receives the repository used to read the students' enrollments.
     */
    public GradeAverageService(StudentCurricularUnitRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Returns the ECTS-weighted average of the graded enrollments, or null if none has a grade.
     */
    public static Double weightedAverage(List<StudentCurricularUnit> enrollments) {
        double weightedSum = 0;
        int totalEcts = 0;
        for (StudentCurricularUnit enrollment : enrollments) {
            if (enrollment.getCuGrade() == null) {
                continue;
            }
            int ects = enrollment.getCurricularUnit().getEcts();
            weightedSum += enrollment.getCuGrade() * ects;
            totalEcts += ects;
        }
        if (totalEcts == 0) {
            return null;
        }
        return BigDecimal.valueOf(weightedSum / totalEcts).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Recalculates and stores the grade average of one student.
     */
    @Transactional
    public void recalculate(Student student) {
        student.setGradeAverage(weightedAverage(enrollmentRepository.findByStudent_IdStudent(student.getIdStudent())));
    }

    /**
     * Recalculates the average of every student enrolled in a curricular unit (e.g. after its ECTS change).
     */
    @Transactional
    public void recalculateForUnit(Long idUnit) {
        enrollmentRepository.findByCurricularUnit_IdUnit(idUnit)
                .forEach(enrollment -> recalculate(enrollment.getStudent()));
    }
}
