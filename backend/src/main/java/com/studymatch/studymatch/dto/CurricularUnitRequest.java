package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data the client sends to create or update a curricular unit. It has no id, so the client cannot control it.
 * The ECTS are optional: when missing, a new unit gets the default and an existing unit keeps its value.
 */
public record CurricularUnitRequest(
        @NotBlank @Size(max = 12) String codeUnit,
        @NotBlank @Size(max = 50) String nameUnit,
        @Min(1) @Max(60) Integer ects
) {

    /**
     * Request without ECTS.
     */
    public CurricularUnitRequest(String codeUnit, String nameUnit) {
        this(codeUnit, nameUnit, null);
    }
}
