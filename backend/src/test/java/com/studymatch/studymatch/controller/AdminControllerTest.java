package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.service.AdminService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private AdminService adminService;
    private MockMvc mockMvc;

    private static final String BODY =
            "{\"nameAdmin\":\"Maria\",\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\"secret\"}";

    @BeforeEach
    void setUp() {
        adminService = Mockito.mock(AdminService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService)).build();
    }

    @Test
    @DisplayName("POST /api/admin returns 201 with the created admin")
    void createAdminReturns201() throws Exception {
        Admin created = new Admin(1L, "Maria", "maria@studymatch.pt", "hash");
        when(adminService.createAdmin(any(Admin.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").value(1))
                .andExpect(jsonPath("$.emailAdmin").value("maria@studymatch.pt"));
    }

    @Test
    @DisplayName("POST /api/admin with duplicate email returns 409")
    void createAdminConflictReturns409() throws Exception {
        when(adminService.createAdmin(any(Admin.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "exists"));

        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/admin with malformed JSON returns 400")
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin returns the list of admins")
    void getAllAdminsReturnsList() throws Exception {
        when(adminService.getAllAdmins()).thenReturn(List.of(
                new Admin(1L, "A", "a@studymatch.pt", "h1"),
                new Admin(2L, "B", "b@studymatch.pt", "h2")));

        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].emailAdmin").value("b@studymatch.pt"));
    }
}
