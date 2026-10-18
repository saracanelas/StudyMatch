package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AdminRepository repository;
    private AuthService service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AdminRepository.class);
        service = new AuthService(repository);
    }

    private Admin adminWithPassword(String rawPassword) {
        Admin admin = new Admin();
        admin.setNameAdmin("Admin");
        admin.setEmailAdmin("admin@studymatch.pt");
        admin.setPasswordAdmin(encoder.encode(rawPassword));
        return admin;
    }

    @Test
    @DisplayName("Login succeeds with correct email and password")
    void validCredentialsReturnTrue() {
        when(repository.findByEmailAdmin("admin@studymatch.pt")).thenReturn(adminWithPassword("secret123"));

        assertTrue(service.login("admin@studymatch.pt", "secret123"));
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void wrongPasswordReturnsFalse() {
        when(repository.findByEmailAdmin("admin@studymatch.pt")).thenReturn(adminWithPassword("secret123"));

        assertFalse(service.login("admin@studymatch.pt", "wrong"));
    }

    @Test
    @DisplayName("Login fails when the email does not exist")
    void unknownEmailReturnsFalse() {
        when(repository.findByEmailAdmin("nobody@studymatch.pt")).thenReturn(null);

        assertFalse(service.login("nobody@studymatch.pt", "secret123"));
    }

    @Test
    @DisplayName("Login fails for null email or password without querying the database")
    void nullInputsReturnFalse() {
        assertFalse(service.login(null, "secret123"));
        assertFalse(service.login("admin@studymatch.pt", null));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Login fails for blank email or password without querying the database")
    void blankInputsReturnFalse() {
        assertFalse(service.login("   ", "secret123"));
        assertFalse(service.login("admin@studymatch.pt", ""));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Stored hash is compared, not the plain text")
    void plainTextStoredPasswordDoesNotMatch() {
        Admin admin = new Admin();
        admin.setEmailAdmin("admin@studymatch.pt");
        admin.setPasswordAdmin("secret123"); // not hashed
        when(repository.findByEmailAdmin("admin@studymatch.pt")).thenReturn(admin);

        assertFalse(service.login("admin@studymatch.pt", "secret123"));
    }
}
