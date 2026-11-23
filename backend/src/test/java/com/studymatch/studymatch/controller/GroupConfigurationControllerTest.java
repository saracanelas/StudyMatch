package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.service.GroupConfigurationService;
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

class GroupConfigurationControllerTest {

    private static final String URL = "/api/units/1/group-configuration";

    private GroupConfigurationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(GroupConfigurationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new GroupConfigurationController(service))
                .setControllerAdvice(new ApiErrorHandler()).build();
    }

    private static GroupConfiguration configuration(int students, List<String> roles) {
        return new GroupConfiguration(new CurricularUnit(1L, "SQ", "Software Quality"), students, roles);
    }

    private void expectBadRequest(String json, String message) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(message));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET returns the configuration with the unit id and no nested unit")
    void getConfiguration() throws Exception {
        when(service.getConfiguration(1L)).thenReturn(configuration(2, List.of("Líder", "Programador")));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUnit").value(1))
                .andExpect(jsonPath("$.numberOfStudents").value(2))
                .andExpect(jsonPath("$.roles[1]").value("Programador"))
                .andExpect(jsonPath("$.curricularUnit").doesNotExist());
    }

    @Test
    @DisplayName("GET returns 404 with { error } when there is no configuration")
    void getConfigurationNotFound() throws Exception {
        when(service.getConfiguration(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sem configuração"));

        mockMvc.perform(get(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sem configuração"));
    }

    @Test
    @DisplayName("POST returns 201 with the created configuration")
    void createConfiguration() throws Exception {
        when(service.createConfiguration(eq(1L), any(GroupConfigurationRequest.class)))
                .thenReturn(configuration(2, List.of("Líder", "Programador")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfStudents\":2,\"roles\":[\"Líder\",\"Programador\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("Líder"));
    }

    @Test
    @DisplayName("POST returns 409 when the unit already has a configuration")
    void createConfigurationConflict() throws Exception {
        when(service.createConfiguration(eq(1L), any(GroupConfigurationRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já existe"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfStudents\":2,\"roles\":[\"A\",\"B\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe"));
    }

    @Test
    @DisplayName("PUT is not allowed: a configuration is final")
    void updateNotAllowed() throws Exception {
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfStudents\":3,\"roles\":[\"A\",\"B\",\"C\"]}"))
                .andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("DELETE returns 204 and deletes the configuration")
    void deleteConfiguration() throws Exception {
        mockMvc.perform(delete(URL)).andExpect(status().isNoContent());
        verify(service).deleteConfiguration(1L);
    }

    @Test
    @DisplayName("DELETE returns 404 with { error } when there is no configuration")
    void deleteConfigurationNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sem configuração"))
                .when(service).deleteConfiguration(1L);

        mockMvc.perform(delete(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sem configuração"));
    }

    @Test
    @DisplayName("POST without the number of students returns 400")
    void missingNumberOfStudents() throws Exception {
        expectBadRequest("{\"roles\":[\"A\",\"B\"]}", "O número de alunos é obrigatório");
    }

    @Test
    @DisplayName("POST with fewer than 2 students returns 400")
    void tooFewStudents() throws Exception {
        expectBadRequest("{\"numberOfStudents\":1,\"roles\":[\"A\"]}", "Cada grupo tem de ter pelo menos 2 alunos");
    }

    @Test
    @DisplayName("POST with more than 10 students returns 400")
    void tooManyStudents() throws Exception {
        expectBadRequest("{\"numberOfStudents\":11,\"roles\":[]}", "Cada grupo pode ter no máximo 10 alunos");
    }

    @Test
    @DisplayName("POST with a different number of roles and students returns 400")
    void rolesNotMatchingStudents() throws Exception {
        expectBadRequest("{\"numberOfStudents\":3,\"roles\":[\"A\",\"B\"]}",
                "O número de roles tem de ser igual ao número de alunos");
    }

    @Test
    @DisplayName("POST with a blank role returns 400")
    void blankRole() throws Exception {
        expectBadRequest("{\"numberOfStudents\":2,\"roles\":[\"A\",\"  \"]}", "Todas as roles têm de ser preenchidas");
    }

    @Test
    @DisplayName("POST without roles returns 400")
    void missingRoles() throws Exception {
        expectBadRequest("{\"numberOfStudents\":2}", "A lista de roles é obrigatória");
    }

    @Test
    @DisplayName("POST with text in the number of students returns 400")
    void malformedBody() throws Exception {
        expectBadRequest("{\"numberOfStudents\":\"dois\",\"roles\":[\"A\",\"B\"]}",
                "Dados inválidos: verifique os campos enviados");
    }
}
