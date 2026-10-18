package com.studymatch.studymatch.service;

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

    private Admin newAdmin(String email) {
        Admin admin = new Admin();
        admin.setNameAdmin("Maria");
        admin.setEmailAdmin(email);
        admin.setPasswordAdmin("plain-password");
        return admin;
    }

    @Test
    @DisplayName("Creating an admin hashes the password before saving")
    void createAdminHashesPassword() {
        when(repository.findByEmailAdmin("maria@studymatch.pt")).thenReturn(null);
        when(repository.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        Admin saved = service.createAdmin(newAdmin("maria@studymatch.pt"));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(repository).save(captor.capture());
        String stored = captor.getValue().getPasswordAdmin();
        assertNotEquals("plain-password", stored);
        assertTrue(new BCryptPasswordEncoder().matches("plain-password", stored));
        assertEquals("maria@studymatch.pt", saved.getEmailAdmin());
    }

    @Test
    @DisplayName("Creating an admin with an existing email returns 409 and does not save")
    void duplicateEmailThrowsConflict() {
        when(repository.findByEmailAdmin("maria@studymatch.pt")).thenReturn(newAdmin("maria@studymatch.pt"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(newAdmin("maria@studymatch.pt")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("A database integrity error is translated to 409")
    void integrityViolationThrowsConflict() {
        when(repository.findByEmailAdmin(any())).thenReturn(null);
        when(repository.save(any(Admin.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(newAdmin("maria@studymatch.pt")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    @DisplayName("getAllAdmins returns what the repository returns")
    void getAllAdminsReturnsRepositoryContent() {
        List<Admin> admins = List.of(newAdmin("a@studymatch.pt"), newAdmin("b@studymatch.pt"));
        when(repository.findAll()).thenReturn(admins);

        assertEquals(2, service.getAllAdmins().size());
        assertSame(admins, service.getAllAdmins());
    }

    @Test
    @DisplayName("getAllAdmins returns an empty list when there are no admins")
    void getAllAdminsEmpty() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.getAllAdmins().isEmpty());
    }
}
