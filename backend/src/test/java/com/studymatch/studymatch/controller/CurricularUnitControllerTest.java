package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.service.CurricularUnitService;
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

class CurricularUnitControllerTest {

    private CurricularUnitService unitService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        unitService = Mockito.mock(CurricularUnitService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CurricularUnitController(unitService)).build();
    }

    private static String body(String code, String name) {
        return "{\"codeUnit\":\"" + code + "\",\"nameUnit\":\"" + name + "\"}";
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }

    @Test
    @DisplayName("GET /api/units returns the list of units without a 'courses' field")
    void getAllUnits() throws Exception {
        when(unitService.getAllCurricularUnits()).thenReturn(List.of(
                new CurricularUnit(1L, "SQ", "Software Quality"),
                new CurricularUnit(2L, "BD", "Databases")));

        mockMvc.perform(get("/api/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codeUnit").value("SQ"))
                .andExpect(jsonPath("$[1].nameUnit").value("Databases"))
                .andExpect(jsonPath("$[0].courses").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/units returns 201 with the created unit")
    void createUnit() throws Exception {
        when(unitService.createCurricularUnit(any(CurricularUnit.class)))
                .thenReturn(new CurricularUnit(1L, "SQ", "Software Quality"));

        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("SQ", "Software Quality")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUnit").value(1))
                .andExpect(jsonPath("$.codeUnit").value("SQ"));
    }

    @Test
    @DisplayName("POST /api/units returns 409 with { error } when the code is taken")
    void createUnitConflict() throws Exception {
        when(unitService.createCurricularUnit(any(CurricularUnit.class)))
                .thenThrow(error(HttpStatus.CONFLICT, "Code already used"));

        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("SQ", "Software Quality")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Code already used"));
    }

    @Test
    @DisplayName("POST /api/units with a blank code returns 400 without calling the service")
    void createUnitBlankCode() throws Exception {
        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("", "Name")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(unitService);
    }

    @Test
    @DisplayName("POST /api/units with a blank name returns 400")
    void createUnitBlankName() throws Exception {
        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("SQ", "")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(unitService);
    }

    @Test
    @DisplayName("POST /api/units with a 13-character code returns 400")
    void createUnitCodeTooLong() throws Exception {
        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("C".repeat(13), "Name")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(unitService);
    }

    @Test
    @DisplayName("POST /api/units with a 51-character name returns 400")
    void createUnitNameTooLong() throws Exception {
        mockMvc.perform(post("/api/units").contentType(MediaType.APPLICATION_JSON).content(body("SQ", "N".repeat(51))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(unitService);
    }

    @Test
    @DisplayName("PUT /api/units/{id} returns 200 with the updated unit")
    void updateUnit() throws Exception {
        when(unitService.updateCurricularUnit(eq(1L), any(CurricularUnit.class)))
                .thenReturn(new CurricularUnit(1L, "NEW", "New name"));

        mockMvc.perform(put("/api/units/1").contentType(MediaType.APPLICATION_JSON).content(body("NEW", "New name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeUnit").value("NEW"))
                .andExpect(jsonPath("$.nameUnit").value("New name"));
    }

    @Test
    @DisplayName("PUT /api/units/{id} returns 404 when the unit does not exist")
    void updateUnitNotFound() throws Exception {
        when(unitService.updateCurricularUnit(eq(9L), any(CurricularUnit.class)))
                .thenThrow(error(HttpStatus.NOT_FOUND, "Unit 9 not found"));

        mockMvc.perform(put("/api/units/9").contentType(MediaType.APPLICATION_JSON).content(body("NEW", "New name")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Unit 9 not found"));
    }

    @Test
    @DisplayName("PUT /api/units/{id} returns 409 when the code belongs to another unit")
    void updateUnitConflict() throws Exception {
        when(unitService.updateCurricularUnit(eq(1L), any(CurricularUnit.class)))
                .thenThrow(error(HttpStatus.CONFLICT, "Code already used"));

        mockMvc.perform(put("/api/units/1").contentType(MediaType.APPLICATION_JSON).content(body("TAKEN", "Name")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Code already used"));
    }

    @Test
    @DisplayName("PUT /api/units/{id} with an invalid body returns 400 without calling the service")
    void updateUnitInvalidBody() throws Exception {
        mockMvc.perform(put("/api/units/1").contentType(MediaType.APPLICATION_JSON).content(body("", "")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(unitService);
    }
}
