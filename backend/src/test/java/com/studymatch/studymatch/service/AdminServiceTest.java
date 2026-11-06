package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.AdminRequest;
import com.studymatch.studymatch.dto.AdminResponse;
import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private AdminRepository repository;
    private AdminService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AdminRepository.class);
        service = new AdminService(repository);
    }

    private AdminRequest newRequest(String email) {
        return new AdminRequest("Maria", email, "plain-password");
    }

    private Admin storedAdmin(Long id, String email) {
        return new Admin(id, "Maria", email, "stored-hash");
    }

    @Test
    @DisplayName("Creating an admin hashes the password before saving")
    void createAdminHashesPassword() {
        when(repository.findByEmailAdmin("maria@studymatch.pt")).thenReturn(null);
        when(repository.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminResponse saved = service.createAdmin(newRequest("maria@studymatch.pt"));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(repository).save(captor.capture());
        String stored = captor.getValue().getPasswordAdmin();
        assertNotEquals("plain-password", stored);
        assertTrue(new BCryptPasswordEncoder().matches("plain-password", stored));
        assertEquals("maria@studymatch.pt", saved.emailAdmin());
    }

    @Test
    @DisplayName("Creating an admin with an existing email returns 409 and does not save")
    void duplicateEmailThrowsConflict() {
        when(repository.findByEmailAdmin("maria@studymatch.pt")).thenReturn(storedAdmin(1L, "maria@studymatch.pt"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(newRequest("maria@studymatch.pt")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("A database integrity error is translated to 409")
    void integrityViolationThrowsConflict() {
        when(repository.findByEmailAdmin(any())).thenReturn(null);
        when(repository.save(any(Admin.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(newRequest("maria@studymatch.pt")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    @DisplayName("getAllAdmins returns the repository content as responses")
    void getAllAdminsReturnsRepositoryContent() {
        when(repository.findAll()).thenReturn(List.of(
                storedAdmin(1L, "a@studymatch.pt"), storedAdmin(2L, "b@studymatch.pt")));

        assertEquals(List.of(
                        new AdminResponse(1L, "Maria", "a@studymatch.pt"),
                        new AdminResponse(2L, "Maria", "b@studymatch.pt")),
                service.getAllAdmins());
    }

    @Test
    @DisplayName("getAllAdmins returns an empty list when there are no admins")
    void getAllAdminsEmpty() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.getAllAdmins().isEmpty());
    }
}
