package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.model.StudentCurricularUnit;
import com.studymatch.studymatch.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EnrollmentControllerTest {

    private EnrollmentService enrollmentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        enrollmentService = Mockito.mock(EnrollmentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new EnrollmentController(enrollmentService))
                .setControllerAdvice(new ValidationErrorHandler())
                .build();
    }

    private static StudentCurricularUnit enrollment(Double grade) {
        StudentCurricularUnit e = new StudentCurricularUnit(
                new Student(1L, "2024001", "Ana", "ana@x.pt", 2024),
                new CurricularUnit(2L, "SQ", "Software Quality"));
        e.setCuGrade(grade);
        return e;
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }

    @Test
    @DisplayName("GET /api/enrollments lists every enrollment")
    void getAll() throws Exception {
        when(enrollmentService.getAllEnrollments()).thenReturn(List.of(enrollment(15.0)));

        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cuGrade").value(15.0))
                .andExpect(jsonPath("$[0].student.numberStudent").value("2024001"))
                .andExpect(jsonPath("$[0].curricularUnit.codeUnit").value("SQ"));
    }

    @Test
    @DisplayName("GET /api/units/{id}/enrollments lists the students enrolled in the unit, or 404")
    void getOfUnit() throws Exception {
        when(enrollmentService.getEnrollmentsOfUnit(2L)).thenReturn(List.of(enrollment(null)));
        when(enrollmentService.getEnrollmentsOfUnit(99L)).thenThrow(error(HttpStatus.NOT_FOUND, "UC 99 não encontrada"));

        mockMvc.perform(get("/api/units/2/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].student.nameStudent").value("Ana"));
        mockMvc.perform(get("/api/units/99/enrollments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("UC 99 não encontrada"));
    }

    @Test
    @DisplayName("GET /api/students/{id}/enrollments lists the units of the student, or 404")
    void getOfStudent() throws Exception {
        when(enrollmentService.getEnrollmentsOfStudent(1L)).thenReturn(List.of(enrollment(12.0)));
        when(enrollmentService.getEnrollmentsOfStudent(99L)).thenThrow(error(HttpStatus.NOT_FOUND, "Estudante 99 não encontrado"));

        mockMvc.perform(get("/api/students/1/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].curricularUnit.ects").value(6));
        mockMvc.perform(get("/api/students/99/enrollments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/enrollments enrolls the student and returns 201")
    void enroll() throws Exception {
        when(enrollmentService.enroll(1L, 2L)).thenReturn(enrollment(null));

        mockMvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"unitId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.curricularUnit.codeUnit").value("SQ"));
    }

    @Test
    @DisplayName("POST /api/enrollments returns 409 with { error } for a duplicate enrollment")
    void enrollDuplicate() throws Exception {
        when(enrollmentService.enroll(1L, 2L)).thenThrow(error(HttpStatus.CONFLICT, "Ana já está inscrito(a) em Software Quality"));

        mockMvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"unitId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ana já está inscrito(a) em Software Quality"));
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id}/grade registers the grade")
    void setGrade() throws Exception {
        when(enrollmentService.setGrade(7L, 16.5)).thenReturn(enrollment(16.5));

        mockMvc.perform(put("/api/enrollments/7/grade").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":16.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuGrade").value(16.5));
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id}/grade returns 400 with { error } for an invalid grade")
    void setGradeInvalid() throws Exception {
        when(enrollmentService.setGrade(7L, 25.0)).thenThrow(error(HttpStatus.BAD_REQUEST, "A nota tem de estar entre 0 e 20"));

        mockMvc.perform(put("/api/enrollments/7/grade").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":25}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A nota tem de estar entre 0 e 20"));
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id}/grade with text instead of a number returns 400 without calling the service")
    void setGradeNotANumber() throws Exception {
        mockMvc.perform(put("/api/enrollments/7/grade").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(enrollmentService);
    }

    @Test
    @DisplayName("DELETE /api/enrollments/{id} returns 204, or 404 with { error }")
    void unenroll() throws Exception {
        doThrow(error(HttpStatus.NOT_FOUND, "Inscrição 99 não encontrada")).when(enrollmentService).unenroll(99L);

        mockMvc.perform(delete("/api/enrollments/7")).andExpect(status().isNoContent());
        verify(enrollmentService).unenroll(7L);
        mockMvc.perform(delete("/api/enrollments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Inscrição 99 não encontrada"));
    }
}
