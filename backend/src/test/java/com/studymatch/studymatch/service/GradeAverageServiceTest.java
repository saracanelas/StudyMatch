package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.repository.StudentCurricularUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GradeAverageServiceTest {

    private StudentCurricularUnitRepository repository;
    private GradeAverageService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(StudentCurricularUnitRepository.class);
        service = new GradeAverageService(repository);
    }

    private static StudentCurricularUnit graded(Student student, int ects, Double grade) {
        CurricularUnit unit = new CurricularUnit(null, "U" + ects, "Unit");
        unit.setEcts(ects);
        StudentCurricularUnit enrollment = new StudentCurricularUnit(student, unit);
        enrollment.setCuGrade(grade);
        return enrollment;
    }

    private static StudentCurricularUnit graded(int ects, Double grade) {
        return graded(null, ects, grade);
    }

    @Test
    @DisplayName("Without enrollments or grades there is no average")
    void noGradesNoAverage() {
        assertNull(GradeAverageService.weightedAverage(List.of()));
        assertNull(GradeAverageService.weightedAverage(List.of(graded(6, null))));
    }

    @Test
    @DisplayName("Units with the same ECTS give the simple average")
    void sameEctsIsSimpleAverage() {
        assertEquals(15.0, GradeAverageService.weightedAverage(List.of(graded(6, 12.0), graded(6, 18.0))));
    }

    @Test
    @DisplayName("Units with more ECTS weigh more: (10x3 + 16x9) / 12 = 14.5")
    void weightedByEcts() {
        assertEquals(14.5, GradeAverageService.weightedAverage(List.of(graded(3, 10.0), graded(9, 16.0))));
    }

    @Test
    @DisplayName("Units without a grade are ignored")
    void ungradedIgnored() {
        assertEquals(17.0, GradeAverageService.weightedAverage(List.of(graded(6, 17.0), graded(12, null))));
    }

    @Test
    @DisplayName("The average is rounded to two decimal places")
    void rounding() {
        // (10 + 11 + 11) / 3 = 10.666... -> 10.67
        assertEquals(10.67, GradeAverageService.weightedAverage(List.of(graded(6, 10.0), graded(6, 11.0), graded(6, 11.0))));
    }

    @Test
    @DisplayName("recalculate stores the average of the student's enrollments")
    void recalculateStoresAverage() {
        Student student = new Student(1L, "1", "Ana", "a@b.pt", 2024);
        when(repository.findByStudent_IdStudent(1L)).thenReturn(List.of(graded(6, 14.0), graded(6, 16.0)));

        service.recalculate(student);

        assertEquals(15.0, student.getGradeAverage());
    }

    @Test
    @DisplayName("recalculateForUnit updates every student enrolled in the unit")
    void recalculateForUnit() {
        Student ana = new Student(1L, "1", "Ana", "a@b.pt", 2024);
        Student rui = new Student(2L, "2", "Rui", "r@b.pt", 2024);
        when(repository.findByCurricularUnit_IdUnit(5L)).thenReturn(List.of(graded(ana, 6, 10.0), graded(rui, 6, 20.0)));
        when(repository.findByStudent_IdStudent(1L)).thenReturn(List.of(graded(6, 10.0)));
        when(repository.findByStudent_IdStudent(2L)).thenReturn(List.of(graded(6, 20.0)));

        service.recalculateForUnit(5L);

        assertEquals(10.0, ana.getGradeAverage());
        assertEquals(20.0, rui.getGradeAverage());
    }
}
