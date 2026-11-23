package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Student number sent by the administrator to associate a student with a curricular unit.
 * The format is checked by the service.
 */
public record EnrollmentRequest(
        @NotBlank(message = "O número de aluno é obrigatório")
        String studentNumber
) {}
