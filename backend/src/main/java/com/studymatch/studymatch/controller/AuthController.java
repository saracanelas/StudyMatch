package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.LoginRequest;
import com.studymatch.studymatch.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest credentials,
                                                     BindingResult validation) {
        // BindingResult keeps the { "error": message } body, which the frontend reads on every failed login
        if (validation.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Email and password are required.")
            );
        }

        boolean authenticated = authService.login(credentials.email(), credentials.password());

        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
        }

        return ResponseEntity.ok(
                Map.of("message", "Login successful.")
        );
    }
}
