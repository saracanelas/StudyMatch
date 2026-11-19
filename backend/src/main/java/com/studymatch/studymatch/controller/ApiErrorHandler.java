package com.studymatch.studymatch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Turns errors of the group controllers into { "error": message } responses the frontend can show.
 */
@RestControllerAdvice(assignableTypes = GroupConfigurationController.class)
public class ApiErrorHandler {

    /**
     * Invalid input: returns 400 with { "error": first message, "details": every message }.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .distinct()
                .toList();
        return ResponseEntity.badRequest().body(Map.of("error", messages.get(0), "details", messages));
    }

    /**
     * Malformed JSON (for example text where a number is expected): returns 400 with a readable message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Dados inválidos: verifique os campos enviados"));
    }

    /**
     * Errors thrown by the services: keeps their status code and message.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatusError(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
    }
}
