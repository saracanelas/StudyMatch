package com.studymatch.studymatch.dto;

/**
 * Body of POST /api/enrollments: the student to enroll and the curricular unit, e.g.
 * { "studentId": 1, "unitId": 2 }.
 */
public record StudentCurricularUnitRequest(Long studentId, Long unitId) {
}
