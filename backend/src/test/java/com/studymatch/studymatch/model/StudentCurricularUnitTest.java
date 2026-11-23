package com.studymatch.studymatch.model;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentCurricularUnitTest {

    @Test
    @DisplayName("A new enrollment links the student and the unit and has no grade")
    void newEnrollment() {
        Student student = new Student(1L, "1", "Ana", "a@b.pt", 2024);
        CurricularUnit unit = new CurricularUnit(2L, "SQ", "Software Quality");

        StudentCurricularUnit enrollment = new StudentCurricularUnit(student, unit);

        assertSame(student, enrollment.getStudent());
        assertSame(unit, enrollment.getCurricularUnit());
        assertNull(enrollment.getCuGrade());
        assertNull(enrollment.getIdEnrollment());
    }

    @Test
    @DisplayName("Grades from 0 to 20 are valid, values outside are not")
    void gradeBoundaries() {
        assertTrue(StudentCurricularUnit.isValidGrade(0));
        assertTrue(StudentCurricularUnit.isValidGrade(9.5));
        assertTrue(StudentCurricularUnit.isValidGrade(20));
        assertFalse(StudentCurricularUnit.isValidGrade(-0.01));
        assertFalse(StudentCurricularUnit.isValidGrade(20.01));
    }

    @Test
    @DisplayName("setCuGrade registers, changes and clears the grade")
    void setGrade() {
        StudentCurricularUnit enrollment = new StudentCurricularUnit();
        enrollment.setCuGrade(14.0);
        assertEquals(14.0, enrollment.getCuGrade());
        enrollment.setCuGrade(null);
        assertNull(enrollment.getCuGrade());
    }

    @Test
    @DisplayName("A curricular unit has 6 ECTS by default and must have between 1 and 60")
    void unitEcts() {
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");
        assertEquals(CurricularUnit.DEFAULT_ECTS, unit.getEcts());

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            unit.setEcts(60);
            assertTrue(validator.validate(unit).isEmpty());
            unit.setEcts(0);
            assertFalse(validator.validate(unit).isEmpty());
            unit.setEcts(61);
            assertFalse(validator.validate(unit).isEmpty());
            unit.setEcts(null);
            assertFalse(validator.validate(unit).isEmpty());
        }
    }
}
