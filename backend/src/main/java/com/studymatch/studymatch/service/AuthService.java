package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.User;
import com.studymatch.studymatch.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Receives the repository used to look up users in the database.
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Logs a user in: the user must exist in the database, the password must match
     * and the user must be marked as admin there. Returns the logged-in user.
     */
    @Transactional(readOnly = true)
    public User login(User credentials) {
        User user = userRepository.findByUsername(credentials.getUsername());
        if (user == null || credentials.getPassword() == null || !passwordsMatch(user.getPassword(), credentials.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este utilizador não é administrador");
        }
        return user;
    }

    /**
     * Compares two passwords in constant time, so the comparison does not leak how many characters matched.
     */
    private boolean passwordsMatch(String storedPassword, String givenPassword) {
        return MessageDigest.isEqual(
                storedPassword.getBytes(StandardCharsets.UTF_8), givenPassword.getBytes(StandardCharsets.UTF_8));
    }
}
