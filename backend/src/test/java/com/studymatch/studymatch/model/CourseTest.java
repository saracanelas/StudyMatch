package com.studymatch.studymatch.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

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
    @DisplayName("Valid course has no violations")
    void validCourse() {
        assertTrue(validator.validate(new Course(null, "LEI", "Computer Engineering")).isEmpty());
    }

    @Test
    @DisplayName("Blank or null code is invalid")
    void blankCodeInvalid() {
        assertFalse(validator.validate(new Course(null, "", "Name")).isEmpty());
        assertFalse(validator.validate(new Course(null, "   ", "Name")).isEmpty());
        assertFalse(validator.validate(new Course(null, null, "Name")).isEmpty());
    }

    @Test
    @DisplayName("Code of 12 characters is valid, 13 is invalid")
    void codeLengthBoundary() {
        assertTrue(validator.validate(new Course(null, "A".repeat(12), "Name")).isEmpty());
        assertFalse(validator.validate(new Course(null, "A".repeat(13), "Name")).isEmpty());
    }

    @Test
    @DisplayName("Blank name is invalid")
    void blankNameInvalid() {
        assertFalse(validator.validate(new Course(null, "LEI", "")).isEmpty());
        assertFalse(validator.validate(new Course(null, "LEI", null)).isEmpty());
    }

    @Test
    @DisplayName("Name of 50 characters is valid, 51 is invalid")
    void nameLengthBoundary() {
        assertTrue(validator.validate(new Course(null, "LEI", "N".repeat(50))).isEmpty());
        assertFalse(validator.validate(new Course(null, "LEI", "N".repeat(51))).isEmpty());
    }

    @Test
    @DisplayName("addCurricularUnit updates both sides of the relation")
    void addKeepsBothSidesConsistent() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");

        course.addCurricularUnit(unit);

        assertTrue(unit.getCourses().contains(course));
    }

    @Test
    @DisplayName("removeCurricularUnit updates both sides of the relation")
    void removeKeepsBothSidesConsistent() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");
        course.addCurricularUnit(unit);

        course.removeCurricularUnit(unit);

        assertFalse(unit.getCourses().contains(course));
    }

    @Test
    @DisplayName("Getters and setters work")
    void gettersAndSetters() {
        Course course = new Course(7L, "OLD", "Old name");
        course.setCodeCourse("NEW");
        course.setNameCourse("New name");

        assertEquals(7L, course.getIdCourse());
        assertEquals("NEW", course.getCodeCourse());
        assertEquals("New name", course.getNameCourse());
    }
}
