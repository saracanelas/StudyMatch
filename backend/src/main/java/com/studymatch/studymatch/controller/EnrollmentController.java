package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.EnrollmentRequest;
import com.studymatch.studymatch.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Students associated with a curricular unit (managed by the administrator).
 * Errors (400 / 404 / 409) are turned into { "error": message } by {@link ApiErrorHandler}.
 */
@RestController
@RequestMapping("/api/units/{idUnit}/students")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Receives the service that holds the enrollment logic.
     */
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * GET /api/units/{idUnit}/students - lists the student numbers of the unit, or 404.
     */
    @GetMapping
    public List<String> getStudents(@PathVariable Long idUnit) {
        return enrollmentService.getStudents(idUnit);
    }

    /**
     * POST /api/units/{idUnit}/students - associates a student with the unit (201), or returns 400 / 404 / 409.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> enrollStudent(@PathVariable Long idUnit, @Valid @RequestBody EnrollmentRequest request) {
        return Map.of("studentNumber", enrollmentService.enrollStudent(idUnit, request.studentNumber()));
    }

    /**
     * DELETE /api/units/{idUnit}/students/{studentNumber} - removes the student and their answer (204), or 404.
     */
    @DeleteMapping("/{studentNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStudent(@PathVariable Long idUnit, @PathVariable String studentNumber) {
        enrollmentService.removeStudent(idUnit, studentNumber);
    }
}
