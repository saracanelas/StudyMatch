package com.studymatch.studymatch.model;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    @DisplayName("New course starts without curricular units")
    void startsWithoutUnits() {
        assertTrue(new Course().getCurricularUnits().isEmpty());
    }

    @Test
    @DisplayName("addCurricularUnit updates both sides of the relation")
    void addKeepsBothSidesConsistent() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");

        course.addCurricularUnit(unit);

        assertTrue(course.getCurricularUnits().contains(unit));
        assertTrue(unit.getCourses().contains(course));
    }

    @Test
    @DisplayName("removeCurricularUnit updates both sides of the relation")
    void removeKeepsBothSidesConsistent() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");
        course.addCurricularUnit(unit);

        course.removeCurricularUnit(unit);

        assertFalse(course.getCurricularUnits().contains(unit));
        assertFalse(unit.getCourses().contains(course));
    }

    @Test
    @DisplayName("removeCurricularUnit with a unit that is not in the course does nothing")
    void removeUnitNotInCourse() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");

        assertDoesNotThrow(() -> course.removeCurricularUnit(unit));
        assertTrue(course.getCurricularUnits().isEmpty());
    }

    @Test
    @DisplayName("The same unit can belong to two courses")
    void unitInTwoCourses() {
        Course lei = new Course(null, "LEI", "Computer Engineering");
        Course lge = new Course(null, "LGE", "Management");
        CurricularUnit unit = new CurricularUnit(null, "MAT", "Mathematics");

        lei.addCurricularUnit(unit);
        lge.addCurricularUnit(unit);

        assertEquals(2, unit.getCourses().size());
        lei.removeCurricularUnit(unit);
        assertEquals(1, unit.getCourses().size());
        assertTrue(unit.getCourses().contains(lge));
    }

    @Test
    @DisplayName("Serializing a course with units does not loop and hides the units' 'courses' field")
    void jsonDoesNotLoop() throws Exception {
        Course course = new Course(1L, "LEI", "Computer Engineering");
        course.addCurricularUnit(new CurricularUnit(2L, "SQ", "Software Quality"));

        String json = new ObjectMapper().writeValueAsString(course);

        assertTrue(json.contains("\"curricularUnits\""));
        assertTrue(json.contains("\"codeUnit\":\"SQ\""));
        assertFalse(json.contains("\"courses\""));
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
