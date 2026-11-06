package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data the client sends to create or update a curricular unit. It has no id, so the client cannot control it.
 */
public record CurricularUnitRequest(
        @NotBlank @Size(max = 12) String codeUnit,
        @NotBlank @Size(max = 50) String nameUnit
) {}
