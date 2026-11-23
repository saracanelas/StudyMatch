package com.studymatch.studymatch.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    private static ValidatorFactory factory;
    private static Validator validator;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() { factory.close(); }

    private static Student valid() {
        return new Student(null, "2024001", "Ana Costa", "ana@studymatch.pt", 2024);
    }

    @Test
    @DisplayName("Valid student has no violations")
    void validStudent() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    @DisplayName("Blank or too long student number is invalid")
    void numberRules() {
        Student s = valid();
        s.setNumberStudent(" ");
        assertFalse(validator.validate(s).isEmpty());
        s.setNumberStudent("1".repeat(20));
        assertTrue(validator.validate(s).isEmpty());
        s.setNumberStudent("1".repeat(21));
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("Blank or too long name is invalid")
    void nameRules() {
        Student s = valid();
        s.setNameStudent(null);
        assertFalse(validator.validate(s).isEmpty());
        s.setNameStudent("N".repeat(100));
        assertTrue(validator.validate(s).isEmpty());
        s.setNameStudent("N".repeat(101));
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("E-mail must be present and well formed")
    void emailRules() {
        Student s = valid();
        s.setEmailStudent("not-an-email");
        assertFalse(validator.validate(s).isEmpty());
        s.setEmailStudent("");
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("Enrollment year is required and must be within the accepted range")
    void enrollmentYearRules() {
        Student s = valid();
        s.setEnrollmentYear(null);
        assertFalse(validator.validate(s).isEmpty());
        s.setEnrollmentYear(Student.MIN_ENROLLMENT_YEAR);
        assertTrue(validator.validate(s).isEmpty());
        s.setEnrollmentYear(Student.MIN_ENROLLMENT_YEAR - 1);
        assertFalse(validator.validate(s).isEmpty());
        s.setEnrollmentYear(Student.MAX_ENROLLMENT_YEAR + 1);
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("Birthday is optional but must be in the past")
    void birthdayRules() {
        Student s = valid();
        s.setBirthday(null);
        assertTrue(validator.validate(s).isEmpty());
        s.setBirthday(LocalDate.of(2004, 5, 1));
        assertTrue(validator.validate(s).isEmpty());
        s.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("Class is optional and limited to 20 characters")
    void classRules() {
        Student s = valid();
        s.setClassStudent("T".repeat(20));
        assertTrue(validator.validate(s).isEmpty());
        s.setClassStudent("T".repeat(21));
        assertFalse(validator.validate(s).isEmpty());
    }

    @Test
    @DisplayName("A new student has no course and no grade average")
    void startsEmpty() {
        Student s = valid();
        assertNull(s.getCourse());
        assertNull(s.getGradeAverage());
    }

    @Test
    @DisplayName("gradeAverage sent by a client is ignored when reading JSON")
    void gradeAverageIsReadOnly() throws Exception {
        String json = "{\"numberStudent\":\"1\",\"nameStudent\":\"Ana\",\"emailStudent\":\"a@b.pt\","
                + "\"enrollmentYear\":2024,\"gradeAverage\":20,\"birthday\":\"2004-05-01\",\"course\":{\"idCourse\":3}}";

        Student s = mapper.readValue(json, Student.class);

        assertNull(s.getGradeAverage());
        assertEquals(LocalDate.of(2004, 5, 1), s.getBirthday());
        assertEquals(3L, s.getCourse().getIdCourse());
    }

    @Test
    @DisplayName("JSON of a student shows the grade average and the course without its units")
    void jsonShowsAverageAndCourseWithoutUnits() throws Exception {
        Student s = valid();
        Course course = new Course(3L, "LEI", "Computer Engineering");
        course.addCurricularUnit(new CurricularUnit(9L, "SQ", "Software Quality"));
        s.setCourse(course);
        s.setGradeAverage(15.5);

        String json = mapper.writeValueAsString(s);

        assertTrue(json.contains("\"gradeAverage\":15.5"));
        assertTrue(json.contains("\"codeCourse\":\"LEI\""));
        assertFalse(json.contains("curricularUnits"));
    }

    @Test
    @DisplayName("Getters and setters work")
    void gettersAndSetters() {
        Student s = new Student(7L, "1", "Old", "old@x.pt", 2020);
        s.setNumberStudent("2");
        s.setNameStudent("New");
        s.setEmailStudent("new@x.pt");
        s.setEnrollmentYear(2021);
        s.setClassStudent("A");

        assertEquals(7L, s.getIdStudent());
        assertEquals("2", s.getNumberStudent());
        assertEquals("New", s.getNameStudent());
        assertEquals("new@x.pt", s.getEmailStudent());
        assertEquals(2021, s.getEnrollmentYear());
        assertEquals("A", s.getClassStudent());
    }
}
