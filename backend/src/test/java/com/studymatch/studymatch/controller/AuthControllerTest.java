package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The controller rejects a request when ANY of these four checks is true:
 *   email == null  ||  email.isBlank()  ||  password == null  ||  password.isBlank()
 * Each check is tested alone (the other fields valid) and then combined (email AND password invalid).
 */
class AuthControllerTest {

    private static final String VALID_EMAIL = "admin@studymatch.pt";
    private static final String VALID_PASSWORD = "secret";

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    private ResultActions login(String json) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // ---- Valid / authentication result ----

    @Test
    @DisplayName("Valid credentials return 200 and call the service with the same email and password")
    void validLoginReturns200() throws Exception {
        when(authService.login(VALID_EMAIL, VALID_PASSWORD)).thenReturn(true);

        login("{\"email\":\"" + VALID_EMAIL + "\",\"password\":\"" + VALID_PASSWORD + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful."));

        verify(authService).login(VALID_EMAIL, VALID_PASSWORD);
    }

    @Test
    @DisplayName("Well-formed but wrong credentials return 401")
    void invalidLoginReturns401() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(false);

        login("{\"email\":\"" + VALID_EMAIL + "\",\"password\":\"wrong\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials."));
    }

    // ---- Each operand of the || condition, alone ----

    static Stream<Arguments> oneInvalidField() {
        return Stream.of(
                Arguments.of("email == null (key missing)", "{\"password\":\"secret\"}"),
                Arguments.of("email == null (explicit null)", "{\"email\":null,\"password\":\"secret\"}"),
                Arguments.of("email.isBlank() (empty)", "{\"email\":\"\",\"password\":\"secret\"}"),
                Arguments.of("email.isBlank() (spaces)", "{\"email\":\"   \",\"password\":\"secret\"}"),
                Arguments.of("email.isBlank() (tab)", "{\"email\":\"\\t\",\"password\":\"secret\"}"),
                Arguments.of("password == null (key missing)", "{\"email\":\"admin@studymatch.pt\"}"),
                Arguments.of("password == null (explicit null)", "{\"email\":\"admin@studymatch.pt\",\"password\":null}"),
                Arguments.of("password.isBlank() (empty)", "{\"email\":\"admin@studymatch.pt\",\"password\":\"\"}"),
                Arguments.of("password.isBlank() (spaces)", "{\"email\":\"admin@studymatch.pt\",\"password\":\"   \"}"),
                Arguments.of("password.isBlank() (tab)", "{\"email\":\"admin@studymatch.pt\",\"password\":\"\\t\"}")
        );
    }

    @ParameterizedTest(name = "{0} returns 400")
    @MethodSource("oneInvalidField")
    @DisplayName("One invalid field alone is enough to return 400")
    void oneInvalidFieldReturns400(String description, String json) throws Exception {
        login(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email and password are required."));

        verifyNoInteractions(authService);
    }

    // ---- Email AND password invalid at the same time ----

    static Stream<Arguments> bothFieldsInvalid() {
        return Stream.of(
                Arguments.of("both missing", "{}"),
                Arguments.of("email null and password null", "{\"email\":null,\"password\":null}"),
                Arguments.of("email null and password blank", "{\"email\":null,\"password\":\"  \"}"),
                Arguments.of("email blank and password null", "{\"email\":\"  \",\"password\":null}"),
                Arguments.of("email blank and password blank", "{\"email\":\"  \",\"password\":\"\"}"),
                Arguments.of("email missing and password blank", "{\"password\":\"\"}"),
                Arguments.of("email blank and password missing", "{\"email\":\"\"}")
        );
    }

    @ParameterizedTest(name = "{0} returns 400")
    @MethodSource("bothFieldsInvalid")
    @DisplayName("Email and password invalid together return 400")
    void bothFieldsInvalidReturn400(String description, String json) throws Exception {
        login(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email and password are required."));

        verifyNoInteractions(authService);
    }

    // ---- All four checks false: the request reaches the service ----

    @Test
    @DisplayName("Fields with spaces inside but not blank are not rejected as blank")
    void notBlankFieldsReachTheService() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(false);

        login("{\"email\":\" a \",\"password\":\" b \"}")
                .andExpect(status().isUnauthorized());

        verify(authService).login(" a ", " b ");
    }

    @Test
    @DisplayName("Malformed JSON returns 400 without calling the service")
    void malformedJsonReturns400() throws Exception {
        login("{not json")
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
