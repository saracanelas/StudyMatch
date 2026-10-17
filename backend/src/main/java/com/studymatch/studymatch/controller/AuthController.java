package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.User;
import com.studymatch.studymatch.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Receives the service that checks the login against the database.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login - receives { username, password } and returns the user if it is an admin.
     */
    @PostMapping("/login")
    public User login(@RequestBody User credentials) {
        return authService.login(credentials);
    }
}
