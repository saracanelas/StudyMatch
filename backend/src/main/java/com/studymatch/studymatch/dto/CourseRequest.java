package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data the client sends to create or update a course. It has no id, so the client cannot control it.
 */
public record CourseRequest(
        @NotBlank @Size(max = 12) String codeCourse,
        @NotBlank @Size(max = 50) String nameCourse
) {}
