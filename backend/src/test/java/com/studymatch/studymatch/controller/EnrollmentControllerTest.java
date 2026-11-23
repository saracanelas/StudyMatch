package com.studymatch.studymatch.controller;

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

    private static final String URL = "/api/units/1/students";

    private EnrollmentService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(EnrollmentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new EnrollmentController(service))
                .setControllerAdvice(new ApiErrorHandler()).build();
    }

    @Test
    @DisplayName("GET returns the student numbers of the unit")
    void getStudents() throws Exception {
        when(service.getStudents(1L)).thenReturn(List.of("111", "222"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("222"));
    }

    @Test
    @DisplayName("POST returns 201 with the enrolled student number")
    void enrollStudent() throws Exception {
        when(service.enrollStudent(1L, "12345")).thenReturn("12345");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"studentNumber\":\"12345\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentNumber").value("12345"));
    }

    @Test
    @DisplayName("POST without a student number returns 400")
    void enrollWithoutNumber() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"studentNumber\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O número de aluno é obrigatório"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST returns 409 with { error } when the student is already in the unit")
    void enrollTwice() throws Exception {
        when(service.enrollStudent(1L, "12345"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já associado"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"studentNumber\":\"12345\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já associado"));
    }

    @Test
    @DisplayName("DELETE returns 204 and removes the student")
    void removeStudent() throws Exception {
        mockMvc.perform(delete(URL + "/12345")).andExpect(status().isNoContent());
        verify(service).removeStudent(1L, "12345");
    }
}
