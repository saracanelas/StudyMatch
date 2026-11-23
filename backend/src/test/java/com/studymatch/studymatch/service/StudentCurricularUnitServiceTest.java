package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.StudentCurricularUnitRepository;
import com.studymatch.studymatch.repository.StudentRepository;
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

class StudentCurricularUnitServiceTest {

    private StudentCurricularUnitRepository enrollmentRepository;
    private StudentRepository studentRepository;
    private CurricularUnitRepository unitRepository;
    private GradeAverageService gradeAverageService;
    private StudentCurricularUnitService service;

    private final Student ana = new Student(1L, "1", "Ana", "a@x.pt", 2024);
    private final CurricularUnit sq = new CurricularUnit(2L, "SQ", "Software Quality");

    @BeforeEach
    void setUp() {
        enrollmentRepository = Mockito.mock(StudentCurricularUnitRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        unitRepository = Mockito.mock(CurricularUnitRepository.class);
        gradeAverageService = Mockito.mock(GradeAverageService.class);
        service = new StudentCurricularUnitService(enrollmentRepository, studentRepository, unitRepository, gradeAverageService);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        when(unitRepository.findById(2L)).thenReturn(Optional.of(sq));
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(StudentCurricularUnit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static ResponseStatusException fails(org.junit.jupiter.api.function.Executable action) {
        return assertThrows(ResponseStatusException.class, action);
    }

    // ---- listing ----

    @Test
    @DisplayName("getAllEnrollments returns what the repository returns")
    void getAll() {
        List<StudentCurricularUnit> all = List.of(new StudentCurricularUnit(ana, sq));
        when(enrollmentRepository.findAll()).thenReturn(all);

        assertSame(all, service.getAllEnrollments());
    }

    @Test
    @DisplayName("Lists the enrollments of a unit and of a student")
    void listByUnitAndStudent() {
        List<StudentCurricularUnit> list = List.of(new StudentCurricularUnit(ana, sq));
        when(enrollmentRepository.findByCurricularUnit_IdUnit(2L)).thenReturn(list);
        when(enrollmentRepository.findByStudent_IdStudent(1L)).thenReturn(list);

        assertSame(list, service.getEnrollmentsOfUnit(2L));
        assertSame(list, service.getEnrollmentsOfStudent(1L));
    }

    @Test
    @DisplayName("Listing for a unit or student that does not exist fails with 404")
    void listMissing() {
        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.getEnrollmentsOfUnit(99L)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.getEnrollmentsOfStudent(99L)).getStatusCode());
    }

    // ---- enroll ----

    @Test
    @DisplayName("enroll saves a new enrollment without a grade")
    void enroll() {
        StudentCurricularUnit result = service.enroll(1L, 2L);

        assertSame(ana, result.getStudent());
        assertSame(sq, result.getCurricularUnit());
        assertNull(result.getCuGrade());
        verify(enrollmentRepository).save(result);
    }

    @Test
    @DisplayName("enroll fails with 400 when an id is missing")
    void enrollMissingIds() {
        assertEquals(HttpStatus.BAD_REQUEST, fails(() -> service.enroll(null, 2L)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, fails(() -> service.enroll(1L, null)).getStatusCode());
    }

    @Test
    @DisplayName("enroll fails with 404 when the student or the unit does not exist")
    void enrollMissingEntities() {
        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.enroll(99L, 2L)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.enroll(1L, 99L)).getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll fails with 409 when the student is already enrolled in the unit")
    void enrollDuplicate() {
        when(enrollmentRepository.existsByStudent_IdStudentAndCurricularUnit_IdUnit(1L, 2L)).thenReturn(true);

        ResponseStatusException ex = fails(() -> service.enroll(1L, 2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ana"));
        verify(enrollmentRepository, never()).save(any());
    }

    // ---- grades ----

    @Test
    @DisplayName("setGrade registers the grade and recalculates the student's average")
    void setGrade() {
        StudentCurricularUnit enrollment = new StudentCurricularUnit(ana, sq);
        when(enrollmentRepository.findById(7L)).thenReturn(Optional.of(enrollment));

        StudentCurricularUnit result = service.setGrade(7L, 17.5);

        assertEquals(17.5, result.getCuGrade());
        verify(gradeAverageService).recalculate(ana);
    }

    @Test
    @DisplayName("setGrade accepts 0, 20 and null (clears the grade)")
    void setGradeBoundaries() {
        StudentCurricularUnit enrollment = new StudentCurricularUnit(ana, sq);
        when(enrollmentRepository.findById(7L)).thenReturn(Optional.of(enrollment));

        assertEquals(0.0, service.setGrade(7L, 0.0).getCuGrade());
        assertEquals(20.0, service.setGrade(7L, 20.0).getCuGrade());
        assertNull(service.setGrade(7L, null).getCuGrade());
    }

    @Test
    @DisplayName("setGrade refuses grades outside 0-20 with 400 without touching the enrollment")
    void setGradeOutOfRange() {
        assertEquals(HttpStatus.BAD_REQUEST, fails(() -> service.setGrade(7L, -1.0)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, fails(() -> service.setGrade(7L, 20.5)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, fails(() -> service.setGrade(7L, Double.NaN)).getStatusCode());
        verifyNoInteractions(enrollmentRepository, gradeAverageService);
    }

    @Test
    @DisplayName("setGrade for an enrollment that does not exist fails with 404")
    void setGradeMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.setGrade(99L, 10.0)).getStatusCode());
    }

    // ---- unenroll ----

    @Test
    @DisplayName("unenroll deletes the enrollment and recalculates the student's average")
    void unenroll() {
        StudentCurricularUnit enrollment = new StudentCurricularUnit(ana, sq);
        when(enrollmentRepository.findById(7L)).thenReturn(Optional.of(enrollment));

        service.unenroll(7L);

        verify(enrollmentRepository).delete(enrollment);
        verify(gradeAverageService).recalculate(ana);
    }

    @Test
    @DisplayName("unenroll of an enrollment that does not exist fails with 404")
    void unenrollMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, fails(() -> service.unenroll(99L)).getStatusCode());
    }
}
