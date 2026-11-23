package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Roles ordered by the student, first one = favourite. Checking that every role of the
 * assignment appears exactly once is done by the service, because it needs the configuration.
 */
public record RolePreferenceRequest(
        @NotEmpty(message = "Tem de ordenar as roles")
        List<@NotBlank(message = "As roles não podem estar vazias") String> rankedRoles
) {
    /**
     * Returns the roles without surrounding spaces.
     */
    public List<String> trimmedRoles() {
        return rankedRoles.stream().map(String::trim).toList();
    }
}
