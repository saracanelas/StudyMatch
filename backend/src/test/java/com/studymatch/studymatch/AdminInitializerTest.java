package com.studymatch.studymatch;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminInitializerTest {

    private AdminRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AdminRepository.class);
    }

    @Test
    @DisplayName("Blank email means no admin is created")
    void blankEmailDoesNothing() {
        new AdminInitializer(repository, "Admin", "", "pass").initializeAdmin();

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Blank password means no admin is created")
    void blankPasswordDoesNothing() {
        new AdminInitializer(repository, "Admin", "admin@studymatch.pt", "  ").initializeAdmin();

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Existing admin is not created again")
    void existingAdminIsNotDuplicated() {
        when(repository.findByEmailAdmin("admin@studymatch.pt")).thenReturn(new Admin());

        new AdminInitializer(repository, "Admin", "admin@studymatch.pt", "pass").initializeAdmin();

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Missing admin is created with a hashed password")
    void missingAdminIsCreated() {
        when(repository.findByEmailAdmin("admin@studymatch.pt")).thenReturn(null);

        new AdminInitializer(repository, "Boss", "admin@studymatch.pt", "pass123").initializeAdmin();

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(repository).save(captor.capture());
        Admin saved = captor.getValue();
        assertEquals("Boss", saved.getNameAdmin());
        assertEquals("admin@studymatch.pt", saved.getEmailAdmin());
        assertNotEquals("pass123", saved.getPasswordAdmin());
        assertTrue(new BCryptPasswordEncoder().matches("pass123", saved.getPasswordAdmin()));
    }
}
