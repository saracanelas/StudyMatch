package com.studymatch.studymatch.dto;

import com.studymatch.studymatch.model.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Data the client sends to create or update a student. It has no id nor grade average,
 * so the client cannot control them. The course is sent as { "idCourse": 1 } or omitted.
 */
public record StudentRequest(
        @NotBlank @Size(max = 20) String numberStudent,
        @NotBlank @Size(max = 100) String nameStudent,
        @NotBlank @Email @Size(max = 100) String emailStudent,
        @Past LocalDate birthday,
        @NotNull @Min(Student.MIN_ENROLLMENT_YEAR) @Max(Student.MAX_ENROLLMENT_YEAR) Integer enrollmentYear,
        @Size(max = 20) String classStudent,
        CourseReference course
) {

    /**
     * Course the student attends, identified only by its id.
     */
    public record CourseReference(Long idCourse) {}

    /**
     * Request with the required fields only (no birthday, class or course).
     */
    public StudentRequest(String numberStudent, String nameStudent, String emailStudent, Integer enrollmentYear) {
        this(numberStudent, nameStudent, emailStudent, null, enrollmentYear, null, null);
    }

    /**
     * Id of the course sent, or null if none was sent.
     */
    public Long idCourse() {
        return course == null ? null : course.idCourse();
    }
}
