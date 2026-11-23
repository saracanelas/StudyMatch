package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentAdminController {

    private final StudentService studentService;

    /**
     * Receives the service that holds the student logic.
     */
    public StudentAdminController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * GET /api/students - lists all students.
     */
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    /**
     * GET /api/students/{id} - returns one student, or 404 with an error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(studentService.getStudentById(id));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * POST /api/students - creates a student; the course is sent as { "course": { "idCourse": 1 } } or omitted.
     * Returns 409 if the number or e-mail is taken and 404 if the course does not exist.
     */
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Student newStudent) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(newStudent));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * PUT /api/students/{id} - changes the data of a student, or returns 404 / 409 with an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody Student updatedStudent) {
        try {
            return ResponseEntity.ok(studentService.updateStudent(id, updatedStudent));
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
