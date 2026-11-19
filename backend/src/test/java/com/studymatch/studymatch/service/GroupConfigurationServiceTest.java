package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    private GroupConfigurationService service;
    private final CurricularUnit unit = new CurricularUnit(1L, "SQ", "Software Quality");

    @BeforeEach
    void setUp() {
        configurationRepository = Mockito.mock(GroupConfigurationRepository.class);
        unitRepository = Mockito.mock(CurricularUnitRepository.class);
        service = new GroupConfigurationService(configurationRepository, unitRepository);
    }

    @Test
    @DisplayName("createConfiguration saves the configuration with trimmed roles")
    void createSaves() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(configurationRepository.findByCurricularUnitIdUnit(1L)).thenReturn(Optional.empty());
        when(configurationRepository.save(any(GroupConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupConfiguration saved = service.createConfiguration(1L,
                new GroupConfigurationRequest(2, List.of(" Líder ", "Programador")));

        assertEquals(1L, saved.getIdUnit());
        assertEquals(2, saved.getNumberOfStudents());
        assertEquals(List.of("Líder", "Programador"), saved.getRoles());
    }

    @Test
    @DisplayName("createConfiguration returns 409 when the unit already has a configuration")
    void createConflict() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(configurationRepository.findByCurricularUnitIdUnit(1L))
                .thenReturn(Optional.of(new GroupConfiguration(unit, 2, List.of("A", "B"))));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createConfiguration(1L, new GroupConfigurationRequest(2, List.of("A", "B"))));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
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
    @DisplayName("updateConfiguration replaces the number of students and the roles")
    void updateReplaces() {
        GroupConfiguration existing = new GroupConfiguration(unit, 2, List.of("A", "B"));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(configurationRepository.findByCurricularUnitIdUnit(1L)).thenReturn(Optional.of(existing));

        GroupConfiguration updated = service.updateConfiguration(1L,
                new GroupConfigurationRequest(3, List.of("X", "Y", "Z")));

        assertSame(existing, updated);
        assertEquals(3, updated.getNumberOfStudents());
        assertEquals(List.of("X", "Y", "Z"), updated.getRoles());
    }

    @Test
    @DisplayName("updateConfiguration returns 404 when the unit has no configuration")
    void updateNotFound() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(configurationRepository.findByCurricularUnitIdUnit(1L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.updateConfiguration(1L, new GroupConfigurationRequest(2, List.of("A", "B"))));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
