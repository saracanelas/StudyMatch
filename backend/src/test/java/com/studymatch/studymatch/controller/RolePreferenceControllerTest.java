package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.model.RolePreference;
import com.studymatch.studymatch.service.RolePreferenceService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RolePreferenceControllerTest {

    private static final String PREFERENCE_URL = "/api/units/1/role-preferences/12345";
    private static final String BODY = "{\"rankedRoles\":[\"Tester\",\"Líder\"]}";

    private RolePreferenceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(RolePreferenceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RolePreferenceController(service))
                .setControllerAdvice(new ApiErrorHandler()).build();
    }

    private static RolePreference preference() {
        GroupConfiguration configuration = new GroupConfiguration(
                new CurricularUnit(1L, "SQ", "Software Quality"), 2, List.of("Líder", "Tester"));
        return new RolePreference(configuration, "12345", List.of("Tester", "Líder"));
    }

    @Test
    @DisplayName("GET /roles returns the available roles")
    void getRoles() throws Exception {
        when(service.getAvailableRoles(1L)).thenReturn(List.of("Líder", "Tester"));

        mockMvc.perform(get("/api/units/1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Líder"));
    }

    @Test
    @DisplayName("GET /roles returns 404 with { error } when there is no configuration")
    void getRolesNotFound() throws Exception {
        when(service.getAvailableRoles(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sem configuração"));

        mockMvc.perform(get("/api/units/1/roles"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sem configuração"));
    }

    @Test
    @DisplayName("GET preference returns the ranking without the nested configuration")
    void getPreference() throws Exception {
        when(service.getPreference(1L, "12345")).thenReturn(preference());

        mockMvc.perform(get(PREFERENCE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber").value("12345"))
                .andExpect(jsonPath("$.rankedRoles[0]").value("Tester"))
                .andExpect(jsonPath("$.idUnit").value(1))
                .andExpect(jsonPath("$.groupConfiguration").doesNotExist());
    }

    @Test
    @DisplayName("PUT returns 201 the first time the student submits")
    void submitCreates() throws Exception {
        when(service.submitPreference(eq(1L), eq("12345"), anyList()))
                .thenReturn(new RolePreferenceService.SubmitResult(preference(), true));

        mockMvc.perform(put(PREFERENCE_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rankedRoles[1]").value("Líder"));
    }

    @Test
    @DisplayName("PUT returns 200 when the student updates the ranking")
    void submitUpdates() throws Exception {
        when(service.submitPreference(eq(1L), eq("12345"), anyList()))
                .thenReturn(new RolePreferenceService.SubmitResult(preference(), false));

        mockMvc.perform(put(PREFERENCE_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT returns 400 with the service message for an invalid ranking")
    void submitInvalidRanking() throws Exception {
        when(service.submitPreference(eq(1L), eq("12345"), anyList()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A role \"Líder\" está repetida"));

        mockMvc.perform(put(PREFERENCE_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A role \"Líder\" está repetida"));
    }

    @Test
    @DisplayName("PUT with an empty ranking returns 400 without calling the service")
    void submitEmptyRanking() throws Exception {
        mockMvc.perform(put(PREFERENCE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"rankedRoles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Tem de ordenar as roles"));
        verifyNoInteractions(service);
    }
}
