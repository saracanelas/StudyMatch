package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    private org.springframework.test.web.servlet.ResultActions login(String json) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    @DisplayName("Valid credentials return 200 and a success message")
    void validLoginReturns200() throws Exception {
        when(authService.login("admin@studymatch.pt", "secret")).thenReturn(true);

        login("{\"email\":\"admin@studymatch.pt\",\"password\":\"secret\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful."));
    }

    @Test
    @DisplayName("Invalid credentials return 401")
    void invalidLoginReturns401() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(false);

        login("{\"email\":\"admin@studymatch.pt\",\"password\":\"wrong\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials."));
    }

    @Test
    @DisplayName("Missing email returns 400 and does not call the service")
    void missingEmailReturns400() throws Exception {
        login("{\"password\":\"secret\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email and password are required."));

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Missing password returns 400")
    void missingPasswordReturns400() throws Exception {
        login("{\"email\":\"admin@studymatch.pt\"}")
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Blank fields return 400")
    void blankFieldsReturn400() throws Exception {
        login("{\"email\":\"  \",\"password\":\"\"}")
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
