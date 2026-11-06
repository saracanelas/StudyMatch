package com.studymatch.studymatch.dto;

import com.studymatch.studymatch.model.Course;

import java.util.List;

/**
 * Course data returned to the client, including its curricular units.
 */
public record CourseResponse(
        Long idCourse,
        String codeCourse,
        String nameCourse,
        List<CurricularUnitResponse> curricularUnits
) {

    /**
     * Builds the response from a course entity. Must run inside a transaction, as the units are loaded lazily.
     */
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getIdCourse(),
                course.getCodeCourse(),
                course.getNameCourse(),
                course.getCurricularUnits().stream().map(CurricularUnitResponse::from).toList()
        );
    }
}
