package com.studymatch.studymatch.dto;

/**
 * Body of PUT /api/enrollments/{id}/grade: the grade from 0 to 20, or null to clear it, e.g. { "grade": 15.5 }.
 */
public record GradeRequest(Double grade) {
}
