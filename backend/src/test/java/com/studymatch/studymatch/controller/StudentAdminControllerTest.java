package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.service.StudentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentAdminControllerTest {

    private StudentService studentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        studentService = Mockito.mock(StudentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentAdminController(studentService))
                .setControllerAdvice(new ValidationErrorHandler())
                .build();
    }

    private static final String VALID_BODY = "{\"numberStudent\":\"2024001\",\"nameStudent\":\"Ana\","
            + "\"emailStudent\":\"ana@studymatch.pt\",\"enrollmentYear\":2024,\"birthday\":\"2004-05-01\"}";

    private static Student ana() {
        return new Student(1L, "2024001", "Ana", "ana@studymatch.pt", 2024);
    }

    @Test
    @DisplayName("GET /api/students returns the list of students")
    void getAll() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(ana()));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numberStudent").value("2024001"));
    }

    @Test
    @DisplayName("GET /api/students/{id} returns the student, or 404 with { error }")
    void getById() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(ana());
        when(studentService.getStudentById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante 99 não encontrado"));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailStudent").value("ana@studymatch.pt"));
        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Estudante 99 não encontrado"));
    }

    @Test
    @DisplayName("POST /api/students creates the student and returns 201")
    void create() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(ana());

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idStudent").value(1));
    }

    @Test
    @DisplayName("POST /api/students returns 409 with { error } when the number is taken")
    void createConflict() throws Exception {
        when(studentService.createStudent(any(Student.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um estudante com o número 2024001"));

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um estudante com o número 2024001"));
    }

    @Test
    @DisplayName("POST /api/students with invalid fields returns 400 naming the fields, without calling the service")
    void createInvalid() throws Exception {
        String body = "{\"numberStudent\":\"\",\"nameStudent\":\"Ana\",\"emailStudent\":\"bad\",\"enrollmentYear\":2024}";

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dados inválidos nos campos: emailStudent, numberStudent"));
        verifyNoInteractions(studentService);
    }

    @Test
    @DisplayName("POST /api/students with malformed JSON returns 400 with { error }")
    void createMalformed() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{\"enrollmentYear\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(studentService);
    }

    @Test
    @DisplayName("PUT /api/students/{id} updates the student, or returns 404 with { error }")
    void update() throws Exception {
        when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(ana());
        when(studentService.updateStudent(eq(99L), any(Student.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante 99 não encontrado"));

        mockMvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameStudent").value("Ana"));
        mockMvc.perform(put("/api/students/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }
}
