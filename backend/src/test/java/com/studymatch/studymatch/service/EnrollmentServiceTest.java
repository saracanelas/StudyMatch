package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.UnitEnrollment;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import com.studymatch.studymatch.repository.UnitEnrollmentRepository;
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

class EnrollmentServiceTest {

    private UnitEnrollmentRepository enrollmentRepository;
    private CurricularUnitRepository unitRepository;
    private GroupConfigurationRepository configurationRepository;
    private RolePreferenceRepository preferenceRepository;
    private NotificationService notificationService;
    private EnrollmentService service;
    private final CurricularUnit unit = new CurricularUnit(1L, "SQ", "Software Quality");

    @BeforeEach
    void setUp() {
        enrollmentRepository = Mockito.mock(UnitEnrollmentRepository.class);
        unitRepository = Mockito.mock(CurricularUnitRepository.class);
        configurationRepository = Mockito.mock(GroupConfigurationRepository.class);
        preferenceRepository = Mockito.mock(RolePreferenceRepository.class);
        notificationService = Mockito.mock(NotificationService.class);
        service = new EnrollmentService(enrollmentRepository, unitRepository, configurationRepository,
                preferenceRepository, notificationService);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
    }

    @Test
    @DisplayName("getStudents returns the student numbers of the unit")
    void getStudents() {
        when(enrollmentRepository.findByCurricularUnitIdUnitOrderByStudentNumber(1L))
                .thenReturn(List.of(new UnitEnrollment(unit, "111"), new UnitEnrollment(unit, "222")));

        assertEquals(List.of("111", "222"), service.getStudents(1L));
    }

    @Test
    @DisplayName("getStudents returns 404 when the unit does not exist")
    void getStudentsUnitNotFound() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.getStudents(9L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    @DisplayName("enrollStudent saves the trimmed number and does not notify while the unit is closed")
    void enrollWithoutConfiguration() {
        assertEquals("12345", service.enrollStudent(1L, " 12345 "));

        verify(enrollmentRepository).save(argThat(e -> e.getStudentNumber().equals("12345")));
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("enrollStudent notifies the student when the unit already has a configuration")
    void enrollWithOpenConfiguration() {
        when(configurationRepository.existsByCurricularUnitIdUnit(1L)).thenReturn(true);

        service.enrollStudent(1L, "12345");

        verify(notificationService).notifyConfigurationOpened(unit, List.of("12345"));
    }

    @Test
    @DisplayName("enrollStudent returns 409 when the student is already in the unit")
    void enrollTwice() {
        when(enrollmentRepository.existsByCurricularUnitIdUnitAndStudentNumber(1L, "12345")).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.enrollStudent(1L, "12345"));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollStudent returns 400 for an invalid student number")
    void enrollInvalidNumber() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.enrollStudent(1L, "12 34"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeStudent deletes the student's answer and the enrollment")
    void removeStudent() {
        UnitEnrollment enrollment = new UnitEnrollment(unit, "12345");
        when(enrollmentRepository.findByCurricularUnitIdUnitAndStudentNumber(1L, "12345"))
                .thenReturn(Optional.of(enrollment));

        service.removeStudent(1L, "12345");

        verify(preferenceRepository).deleteByGroupConfigurationCurricularUnitIdUnitAndStudentNumber(1L, "12345");
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    @DisplayName("removeStudent returns 404 when the student is not in the unit")
    void removeUnknownStudent() {
        when(enrollmentRepository.findByCurricularUnitIdUnitAndStudentNumber(1L, "12345"))
                .thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.removeStudent(1L, "12345"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    @DisplayName("getOpenUnits returns only the student's units with an open group configuration")
    void openUnitsOnly() {
        CurricularUnit closedUnit = new CurricularUnit(2L, "BD", "Bases de Dados");
        CurricularUnit otherOpenUnit = new CurricularUnit(3L, "AL", "Algoritmos");
        when(enrollmentRepository.findByStudentNumber("12345")).thenReturn(List.of(
                new UnitEnrollment(unit, "12345"),
                new UnitEnrollment(closedUnit, "12345"),
                new UnitEnrollment(otherOpenUnit, "12345")));
        when(configurationRepository.existsByCurricularUnitIdUnit(1L)).thenReturn(true);
        when(configurationRepository.existsByCurricularUnitIdUnit(2L)).thenReturn(false);
        when(configurationRepository.existsByCurricularUnitIdUnit(3L)).thenReturn(true);

        List<CurricularUnitResponse> openUnits = service.getOpenUnits("12345");

        assertEquals(List.of("AL", "SQ"), openUnits.stream().map(CurricularUnitResponse::codeUnit).toList());
    }
}
