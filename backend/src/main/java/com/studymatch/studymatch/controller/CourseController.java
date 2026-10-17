package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * GET /api/courses/{id} - returns one course.
     */
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    /**
     * POST /api/courses - creates a course from its code and name.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(@Valid @RequestBody Course newCourse) {
        return courseService.createCourse(newCourse);
    }

    /**
     * PUT /api/courses/{id} - changes the code and name of a course.
     */
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @Valid @RequestBody Course updatedCourse) {
        return courseService.updateCourse(id, updatedCourse);
    }

    /**
     * PUT /api/courses/{id}/units - sets the curricular units of a course from a list of unit ids, e.g. [1, 2].
     */
    @PutMapping("/{id}/units")
    public Course setCourseUnits(@PathVariable Long id, @RequestBody List<Long> unitIds) {
        return courseService.setCourseUnits(id, unitIds);
    }
}
