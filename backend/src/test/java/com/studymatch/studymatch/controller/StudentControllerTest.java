package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Notification;
import com.studymatch.studymatch.service.EnrollmentService;
import com.studymatch.studymatch.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentControllerTest {

    private EnrollmentService enrollmentService;
    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        enrollmentService = Mockito.mock(EnrollmentService.class);
        notificationService = Mockito.mock(NotificationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(enrollmentService, notificationService))
                .setControllerAdvice(new ApiErrorHandler()).build();
    }

    @Test
    @DisplayName("GET /units returns the open units of the student")
    void getOpenUnits() throws Exception {
        when(enrollmentService.getOpenUnits("12345"))
                .thenReturn(List.of(new CurricularUnitResponse(1L, "SQ", "Software Quality")));

        mockMvc.perform(get("/api/students/12345/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeUnit").value("SQ"));
    }

    @Test
    @DisplayName("GET /notifications returns the notifications without the nested unit")
    void getNotifications() throws Exception {
        Notification notification = new Notification("12345", new CurricularUnit(1L, "SQ", "Software Quality"), "Olá");
        when(notificationService.getNotifications("12345")).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/students/12345/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Olá"))
                .andExpect(jsonPath("$[0].idUnit").value(1))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].curricularUnit").doesNotExist());
    }

    @Test
    @DisplayName("PATCH /notifications/{id}/read returns 404 with { error } for an unknown notification")
    void markAsReadNotFound() throws Exception {
        when(notificationService.markAsRead("12345", 7L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação 7 não encontrada"));

        mockMvc.perform(patch("/api/students/12345/notifications/7/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Notificação 7 não encontrada"));
    }
}
