package com.studymatch.studymatch.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurricularUnitTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() { factory.close(); }

    @Test
    @DisplayName("Valid unit has no violations")
    void validUnit() {
        assertTrue(validator.validate(new CurricularUnit(null, "SQ", "Software Quality")).isEmpty());
    }

    @Test
    @DisplayName("Blank or null code is invalid")
    void blankCodeInvalid() {
        assertFalse(validator.validate(new CurricularUnit(null, "", "Name")).isEmpty());
        assertFalse(validator.validate(new CurricularUnit(null, null, "Name")).isEmpty());
    }

    @Test
    @DisplayName("Code of 12 characters is valid, 13 is invalid")
    void codeLengthBoundary() {
        assertTrue(validator.validate(new CurricularUnit(null, "C".repeat(12), "Name")).isEmpty());
        assertFalse(validator.validate(new CurricularUnit(null, "C".repeat(13), "Name")).isEmpty());
    }

    @Test
    @DisplayName("Blank name is invalid")
    void blankNameInvalid() {
        assertFalse(validator.validate(new CurricularUnit(null, "SQ", " ")).isEmpty());
    }

    @Test
    @DisplayName("Name of 50 characters is valid, 51 is invalid")
    void nameLengthBoundary() {
        assertTrue(validator.validate(new CurricularUnit(null, "SQ", "N".repeat(50))).isEmpty());
        assertFalse(validator.validate(new CurricularUnit(null, "SQ", "N".repeat(51))).isEmpty());
    }

    @Test
    @DisplayName("New unit starts with an empty list of courses")
    void startsWithNoCourses() {
        assertTrue(new CurricularUnit().getCourses().isEmpty());
    }

    @Test
    @DisplayName("Getters and setters work")
    void gettersAndSetters() {
        CurricularUnit unit = new CurricularUnit(3L, "OLD", "Old");
        unit.setCodeUnit("NEW");
        unit.setNameUnit("New");

        assertEquals(3L, unit.getIdUnit());
        assertEquals("NEW", unit.getCodeUnit());
        assertEquals("New", unit.getNameUnit());
    }
}
