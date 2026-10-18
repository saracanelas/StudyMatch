package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    /**
     * Receives the service that holds the course logic.
     */
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses - lists all courses with their curricular units.
     */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    /**
     * GET /api/courses/{id} - returns one course, or 404 with an error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(courseService.getCourseById(id));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * POST /api/courses - creates a course from its code and name, or returns 409 if the code is taken.
     */
    @PostMapping
    public ResponseEntity<?> createCourse(@Valid @RequestBody Course newCourse) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(newCourse));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * PUT /api/courses/{id} - changes the code and name of a course, or returns 404 / 409 with an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @Valid @RequestBody Course updatedCourse) {
        try {
            return ResponseEntity.ok(courseService.updateCourse(id, updatedCourse));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * PUT /api/courses/{id}/units - sets the curricular units of a course from a list of unit ids, e.g. [1, 2].
     */
    @PutMapping("/{id}/units")
    public ResponseEntity<?> setCourseUnits(@PathVariable Long id, @RequestBody List<Long> unitIds) {
        try {
            return ResponseEntity.ok(courseService.setCourseUnits(id, unitIds));
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
