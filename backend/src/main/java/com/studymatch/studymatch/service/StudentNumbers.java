package com.studymatch.studymatch.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

/**
 * Checks student numbers. Temporary: students are identified by their number until they have an entity.
 */
final class StudentNumbers {

    private static final Pattern STUDENT_NUMBER = Pattern.compile("[A-Za-z0-9]{1,20}");

    private StudentNumbers() {}

    /**
     * Returns the number without surrounding spaces, or throws 400 if it is not 1-20 letters or digits.
     */
    static String validate(String studentNumber) {
        String trimmed = studentNumber == null ? null : studentNumber.trim();
        if (trimmed == null || !STUDENT_NUMBER.matcher(trimmed).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O número de aluno só pode ter letras e números (máximo 20)");
        }
        return trimmed;
    }
}
