package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.EnrollmentRequest;
import com.studymatch.studymatch.dto.GradeRequest;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Enrollments of students in curricular units (Student_CU) and their grades.
 */
@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Receives the service that holds the enrollment logic.
     */
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * GET /api/enrollments - lists every enrollment with its student, unit and grade.
     */
    @GetMapping("/enrollments")
    public List<StudentCurricularUnit> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    /**
     * GET /api/units/{id}/enrollments - lists the students enrolled in a curricular unit, with their grades.
     */
    @GetMapping("/units/{id}/enrollments")
    public ResponseEntity<?> getEnrollmentsOfUnit(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsOfUnit(id));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * GET /api/students/{id}/enrollments - lists the curricular units a student is enrolled in, with the grades.
     */
    @GetMapping("/students/{id}/enrollments")
    public ResponseEntity<?> getEnrollmentsOfStudent(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsOfStudent(id));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * POST /api/enrollments - enrolls a student in a curricular unit, e.g. { "studentId": 1, "unitId": 2 }.
     * Returns 404 if the student or unit does not exist and 409 if the student is already enrolled.
     */
    @PostMapping("/enrollments")
    public ResponseEntity<?> enroll(@RequestBody EnrollmentRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(enrollmentService.enroll(request.studentId(), request.unitId()));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * PUT /api/enrollments/{id}/grade - registers or updates the grade, e.g. { "grade": 15.5 } ({ "grade": null } clears it).
     * Returns 400 if the grade is not between 0 and 20 and 404 if the enrollment does not exist.
     */
    @PutMapping("/enrollments/{id}/grade")
    public ResponseEntity<?> setGrade(@PathVariable Long id, @RequestBody GradeRequest request) {
        try {
            return ResponseEntity.ok(enrollmentService.setGrade(id, request.grade()));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * DELETE /api/enrollments/{id} - removes an enrollment (and its grade).
     */
    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<?> unenroll(@PathVariable Long id) {
        try {
            enrollmentService.unenroll(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * Turns an error thrown by the service into a response with its status code and { "error": message }.
     */
    private ResponseEntity<Map<String, String>> errorResponse(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
    }
}
