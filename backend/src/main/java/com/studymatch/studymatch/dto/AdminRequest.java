package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data the client sends to create an administrator. It has no id, so the client cannot control it.
 */
public record AdminRequest(
        @NotBlank @Size(max = 100) String nameAdmin,
        @NotBlank @Email @Size(max = 100) String emailAdmin,
        @NotBlank String passwordAdmin
) {}
