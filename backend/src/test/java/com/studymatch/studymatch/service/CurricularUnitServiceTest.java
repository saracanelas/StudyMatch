package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.CurricularUnitRequest;
import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurricularUnitServiceTest {

    private CurricularUnitRepository repository;
    private CurricularUnitService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CurricularUnitRepository.class);
        service = new CurricularUnitService(repository);
    }

    private CurricularUnit unit(Long id, String code, String name) {
        return new CurricularUnit(id, code, name);
    }

    @Test
    @DisplayName("getAllCurricularUnits returns the repository content as responses")
    void getAll() {
        when(repository.findAll()).thenReturn(List.of(unit(1L, "SQ", "Software Quality"), unit(2L, "BD", "Databases")));

        assertEquals(List.of(
                        new CurricularUnitResponse(1L, "SQ", "Software Quality"),
                        new CurricularUnitResponse(2L, "BD", "Databases")),
                service.getAllCurricularUnits());
    }

    @Test
    @DisplayName("createCurricularUnit saves a new unit without an id, leaving it to the database")
    void createSavesWithoutClientId() {
        when(repository.findByCodeUnit("SQ")).thenReturn(null);
        when(repository.save(any(CurricularUnit.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createCurricularUnit(new CurricularUnitRequest("SQ", "Software Quality"));

        ArgumentCaptor<CurricularUnit> captor = ArgumentCaptor.forClass(CurricularUnit.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getIdUnit());
        assertEquals("SQ", captor.getValue().getCodeUnit());
        assertEquals("Software Quality", captor.getValue().getNameUnit());
    }

    @Test
    @DisplayName("createCurricularUnit with an existing code throws 409 and does not save")
    void createDuplicateCode() {
        when(repository.findByCodeUnit("SQ")).thenReturn(unit(1L, "SQ", "Existing"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createCurricularUnit(new CurricularUnitRequest("SQ", "New")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("SQ"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateCurricularUnit throws 404 when the unit does not exist")
    void updateNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateCurricularUnit(9L, new CurricularUnitRequest("NEW", "New")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("9"));
        verify(repository, never()).findByCodeUnit(any());
    }

    @Test
    @DisplayName("updateCurricularUnit with a code used by ANOTHER unit throws 409 and changes nothing")
    void updateCodeUsedByOther() {
        CurricularUnit target = unit(1L, "OLD", "Old name");
        when(repository.findById(1L)).thenReturn(Optional.of(target));
        when(repository.findByCodeUnit("TAKEN")).thenReturn(unit(2L, "TAKEN", "Other"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateCurricularUnit(1L, new CurricularUnitRequest("TAKEN", "New name")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("OLD", target.getCodeUnit());
        assertEquals("Old name", target.getNameUnit());
    }

    @Test
    @DisplayName("updateCurricularUnit may keep the unit's own code and change only the name")
    void updateKeepsOwnCode() {
        CurricularUnit target = unit(1L, "SQ", "Old name");
        when(repository.findById(1L)).thenReturn(Optional.of(target));
        when(repository.findByCodeUnit("SQ")).thenReturn(target);

        CurricularUnitResponse result = service.updateCurricularUnit(1L, new CurricularUnitRequest("SQ", "New name"));

        assertEquals(new CurricularUnitResponse(1L, "SQ", "New name"), result);
        assertEquals("SQ", target.getCodeUnit());
        assertEquals("New name", target.getNameUnit());
    }

    @Test
    @DisplayName("updateCurricularUnit with a free code changes code and name")
    void updateWithFreeCode() {
        CurricularUnit target = unit(1L, "OLD", "Old name");
        when(repository.findById(1L)).thenReturn(Optional.of(target));
        when(repository.findByCodeUnit("NEW")).thenReturn(null);

        CurricularUnitResponse result = service.updateCurricularUnit(1L, new CurricularUnitRequest("NEW", "New name"));

        assertEquals(new CurricularUnitResponse(1L, "NEW", "New name"), result);
        assertEquals("NEW", target.getCodeUnit());
        assertEquals("New name", target.getNameUnit());
    }
}
