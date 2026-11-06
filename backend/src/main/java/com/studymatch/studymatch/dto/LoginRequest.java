package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credentials the client sends to log in.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {}
