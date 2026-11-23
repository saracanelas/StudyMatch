package com.studymatch.studymatch.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data sent by the teacher to create or update a group configuration.
 */
public record GroupConfigurationRequest(
        @NotNull(message = "O número de alunos é obrigatório")
        @Min(value = MIN_STUDENTS, message = "Cada grupo tem de ter pelo menos " + MIN_STUDENTS + " alunos")
        @Max(value = MAX_STUDENTS, message = "Cada grupo pode ter no máximo " + MAX_STUDENTS + " alunos")
        Integer numberOfStudents,

        @NotNull(message = "A lista de roles é obrigatória")
        List<@NotBlank(message = "Todas as roles têm de ser preenchidas")
             @Size(max = 50, message = "Cada role pode ter no máximo 50 caracteres") String> roles
) {
    public static final int MIN_STUDENTS = 2;
    public static final int MAX_STUDENTS = 10;

    /**
     * Each member of the group needs exactly one role. Only checked when the number of students
     * is itself valid, so the teacher sees the more basic error first.
     */
    @AssertTrue(message = "O número de roles tem de ser igual ao número de alunos")
    public boolean isRolesMatchingStudents() {
        if (numberOfStudents == null || roles == null
                || numberOfStudents < MIN_STUDENTS || numberOfStudents > MAX_STUDENTS) {
            return true;
        }
        return roles.size() == numberOfStudents;
    }

    /**
     * Returns the roles without surrounding spaces.
     */
    public List<String> trimmedRoles() {
        return roles.stream().map(String::trim).toList();
    }
}
