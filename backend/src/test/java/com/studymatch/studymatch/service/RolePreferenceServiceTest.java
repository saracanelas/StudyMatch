package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.model.RolePreference;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RolePreferenceServiceTest {

    private RolePreferenceRepository preferenceRepository;
    private GroupConfigurationService configurationService;
    private EnrollmentService enrollmentService;
    private RolePreferenceService service;

    // Configuration with a repeated role: the student ranks each role only once
    private final GroupConfiguration configuration = new GroupConfiguration(
            new CurricularUnit(1L, "SQ", "Software Quality"), 4, List.of("Líder", "Programador", "Programador", "Tester"));

    @BeforeEach
    void setUp() {
        preferenceRepository = Mockito.mock(RolePreferenceRepository.class);
        configurationService = Mockito.mock(GroupConfigurationService.class);
        enrollmentService = Mockito.mock(EnrollmentService.class);
        service = new RolePreferenceService(preferenceRepository, configurationService, enrollmentService);
        when(configurationService.getConfiguration(1L)).thenReturn(configuration);
        when(enrollmentService.isEnrolled(1L, "12345")).thenReturn(true);
    }

    private void expectBadRequest(List<String> ranking, String message) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.submitPreference(1L, "12345", ranking));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals(message, e.getReason());
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAvailableRoles returns each role once, in the teacher's order")
    void availableRolesWithoutDuplicates() {
        assertEquals(List.of("Líder", "Programador", "Tester"), service.getAvailableRoles(1L));
    }

    @Test
    @DisplayName("getAvailableRoles returns 404 when the unit has no configuration")
    void availableRolesWithoutConfiguration() {
        when(configurationService.getConfiguration(2L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sem configuração"));

        assertThrows(ResponseStatusException.class, () -> service.getAvailableRoles(2L));
    }

    @Test
    @DisplayName("submitPreference creates a new preference the first time")
    void submitCreates() {
        when(preferenceRepository.findByGroupConfigurationIdConfigurationAndStudentNumber(any(), eq("12345")))
                .thenReturn(Optional.empty());
        when(preferenceRepository.save(any(RolePreference.class))).thenAnswer(inv -> inv.getArgument(0));

        RolePreferenceService.SubmitResult result =
                service.submitPreference(1L, "12345", List.of("Tester", "Líder", "Programador"));

        assertTrue(result.created());
        assertEquals("12345", result.preference().getStudentNumber());
        assertEquals(List.of("Tester", "Líder", "Programador"), result.preference().getRankedRoles());
        assertEquals(1L, result.preference().getIdUnit());
    }

    @Test
    @DisplayName("submitPreference replaces the ranking of an existing preference")
    void submitUpdates() {
        RolePreference existing = new RolePreference(configuration, "12345", List.of("Líder", "Programador", "Tester"));
        when(preferenceRepository.findByGroupConfigurationIdConfigurationAndStudentNumber(any(), eq("12345")))
                .thenReturn(Optional.of(existing));

        RolePreferenceService.SubmitResult result =
                service.submitPreference(1L, "12345", List.of("Programador", "Tester", "Líder"));

        assertFalse(result.created());
        assertSame(existing, result.preference());
        assertEquals(List.of("Programador", "Tester", "Líder"), existing.getRankedRoles());
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitPreference refuses a repeated role")
    void repeatedRole() {
        expectBadRequest(List.of("Líder", "Líder", "Tester"), "A role \"Líder\" está repetida");
    }

    @Test
    @DisplayName("submitPreference refuses a role that is not in the assignment")
    void unknownRole() {
        expectBadRequest(List.of("Líder", "Designer", "Tester"), "A role \"Designer\" não existe neste trabalho");
    }

    @Test
    @DisplayName("submitPreference refuses a ranking that leaves roles out")
    void missingRoles() {
        expectBadRequest(List.of("Tester"), "Faltam ordenar as roles: Líder, Programador");
    }

    @Test
    @DisplayName("submitPreference refuses an invalid student number")
    void invalidStudentNumber() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.submitPreference(1L, "12 34", List.of("Líder", "Programador", "Tester")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    @Test
    @DisplayName("getPreference returns 404 when the student has not submitted yet")
    void getPreferenceNotFound() {
        when(preferenceRepository.findByGroupConfigurationIdConfigurationAndStudentNumber(any(), eq("12345")))
                .thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.getPreference(1L, "12345"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    @DisplayName("submitPreference returns 403 when the student does not belong to the unit")
    void submitNotEnrolled() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.submitPreference(1L, "99999", List.of("Líder", "Programador", "Tester")));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPreference returns 403 when the student does not belong to the unit")
    void getPreferenceNotEnrolled() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.getPreference(1L, "99999"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }
}
