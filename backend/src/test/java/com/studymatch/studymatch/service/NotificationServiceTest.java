package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Notification;
import com.studymatch.studymatch.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService service;
    private final CurricularUnit unit = new CurricularUnit(1L, "SQ", "Software Quality");

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(NotificationRepository.class);
        service = new NotificationService(repository);
    }

    @Test
    @DisplayName("notifyConfigurationOpened creates one unread notification per student")
    @SuppressWarnings("unchecked")
    void notifiesEveryStudent() {
        service.notifyConfigurationOpened(unit, List.of("111", "222"));

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<Notification> saved = captor.getValue();
        assertEquals(List.of("111", "222"), saved.stream().map(Notification::getStudentNumber).toList());
        assertTrue(saved.stream().noneMatch(Notification::isRead));
        assertTrue(saved.get(0).getMessage().contains("SQ"));
        assertEquals(1L, saved.get(0).getIdUnit());
    }

    @Test
    @DisplayName("getNotifications returns the student's notifications")
    void getNotifications() {
        Notification notification = new Notification("111", unit, "Olá");
        when(repository.findByStudentNumberOrderByCreatedAtDescIdNotificationDesc("111"))
                .thenReturn(List.of(notification));

        assertEquals(List.of(notification), service.getNotifications("111"));
    }

    @Test
    @DisplayName("markAsRead marks the student's notification as read")
    void markAsRead() {
        Notification notification = new Notification("111", unit, "Olá");
        when(repository.findByIdNotificationAndStudentNumber(5L, "111")).thenReturn(Optional.of(notification));

        assertTrue(service.markAsRead("111", 5L).isRead());
    }

    @Test
    @DisplayName("markAsRead returns 404 for a notification of another student")
    void markAsReadNotFound() {
        when(repository.findByIdNotificationAndStudentNumber(5L, "222")).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.markAsRead("222", 5L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
