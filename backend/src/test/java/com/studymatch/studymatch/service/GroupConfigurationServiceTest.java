package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupConfigurationServiceTest {

    private GroupConfigurationRepository configurationRepository;
    private CurricularUnitRepository unitRepository;
    private RolePreferenceRepository preferenceRepository;
    private EnrollmentService enrollmentService;
    private NotificationService notificationService;
    private GroupConfigurationService service;
    private final CurricularUnit unit = new CurricularUnit(1L, "SQ", "Software Quality");

    @BeforeEach
    void setUp() {
        configurationRepository = Mockito.mock(GroupConfigurationRepository.class);
        unitRepository = Mockito.mock(CurricularUnitRepository.class);
        preferenceRepository = Mockito.mock(RolePreferenceRepository.class);
        enrollmentService = Mockito.mock(EnrollmentService.class);
        notificationService = Mockito.mock(NotificationService.class);
        service = new GroupConfigurationService(configurationRepository, unitRepository, preferenceRepository,
                enrollmentService, notificationService);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
    }

    @Test
    @DisplayName("createConfiguration saves the configuration with trimmed roles")
    void createSaves() {
        when(enrollmentService.countStudents(1L)).thenReturn(2L);
        when(configurationRepository.save(any(GroupConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupConfiguration saved = service.createConfiguration(1L,
                new GroupConfigurationRequest(2, List.of(" Líder ", "Programador")));

        assertEquals(1L, saved.getIdUnit());
        assertEquals(2, saved.getNumberOfStudents());
        assertEquals(List.of("Líder", "Programador"), saved.getRoles());
    }

    @Test
    @DisplayName("createConfiguration notifies every student of the unit")
    void createNotifiesStudents() {
        when(enrollmentService.countStudents(1L)).thenReturn(3L);
        when(enrollmentService.getStudents(1L)).thenReturn(List.of("111", "222", "333"));
        when(configurationRepository.save(any(GroupConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createConfiguration(1L, new GroupConfigurationRequest(2, List.of("A", "B")));

        verify(notificationService).notifyConfigurationOpened(unit, List.of("111", "222", "333"));
    }

    @Test
    @DisplayName("createConfiguration accepts groups exactly as big as the number of students of the unit")
    void createWithAsManyStudentsAsTheGroup() {
        when(enrollmentService.countStudents(1L)).thenReturn(3L);
        when(configurationRepository.save(any(GroupConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.createConfiguration(1L,
                new GroupConfigurationRequest(3, List.of("A", "B", "C"))));
    }

    @Test
    @DisplayName("createConfiguration returns 400 when the unit has fewer students than one group")
    void createWithTooFewStudentsInUnit() {
        when(enrollmentService.countStudents(1L)).thenReturn(2L);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createConfiguration(1L, new GroupConfigurationRequest(3, List.of("A", "B", "C"))));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals("A UC SQ tem 2 aluno(s) associado(s); não é possível formar grupos de 3 alunos", e.getReason());
        verify(configurationRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("createConfiguration returns 400 when the unit has no students")
    void createWithoutStudents() {
        when(enrollmentService.countStudents(1L)).thenReturn(0L);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createConfiguration(1L, new GroupConfigurationRequest(2, List.of("A", "B"))));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(configurationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createConfiguration returns 409 when the unit already has a configuration (it is final)")
    void createConflict() {
        when(configurationRepository.existsByCurricularUnitIdUnit(1L)).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createConfiguration(1L, new GroupConfigurationRequest(2, List.of("A", "B"))));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        assertTrue(e.getReason().contains("administrador"));
        verify(configurationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createConfiguration returns 404 when the unit does not exist")
    void createUnitNotFound() {
        when(unitRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createConfiguration(9L, new GroupConfigurationRequest(2, List.of("A", "B"))));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    @DisplayName("deleteConfiguration deletes every answer and then the configuration")
    void deleteRemovesAnswersAndConfiguration() {
        GroupConfiguration existing = new GroupConfiguration(unit, 2, List.of("A", "B"));
        when(configurationRepository.findByCurricularUnitIdUnit(1L)).thenReturn(Optional.of(existing));

        service.deleteConfiguration(1L);

        InOrder inOrder = inOrder(preferenceRepository, configurationRepository);
        inOrder.verify(preferenceRepository).deleteByGroupConfigurationIdConfiguration(existing.getIdConfiguration());
        inOrder.verify(configurationRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteConfiguration returns 404 when the unit has no configuration")
    void deleteNotFound() {
        when(configurationRepository.findByCurricularUnitIdUnit(1L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.deleteConfiguration(1L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        verify(configurationRepository, never()).delete(any());
    }
}
