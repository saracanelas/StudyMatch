package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.Notification;
import com.studymatch.studymatch.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Receives the repository used to read and save notifications.
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Tells each of the given students that the group configuration of the unit is open.
     */
    @Transactional
    public void notifyConfigurationOpened(CurricularUnit unit, Collection<String> studentNumbers) {
        String message = "Foi aberta a configuração de grupos da UC " + unit.getCodeUnit() + " - "
                + unit.getNameUnit() + ". Já pode ordenar as suas preferências de roles.";
        List<Notification> notifications = studentNumbers.stream()
                .map(studentNumber -> new Notification(studentNumber, unit, message))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    /**
     * Returns the notifications of a student, newest first.
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(String studentNumber) {
        return notificationRepository.findByStudentNumberOrderByCreatedAtDescIdNotificationDesc(
                StudentNumbers.validate(studentNumber));
    }

    /**
     * Marks a notification of the student as read, or returns 404 if it is not theirs.
     */
    @Transactional
    public Notification markAsRead(String studentNumber, Long idNotification) {
        Notification notification = notificationRepository
                .findByIdNotificationAndStudentNumber(idNotification, StudentNumbers.validate(studentNumber))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Notificação " + idNotification + " não encontrada"));
        notification.markAsRead();
        return notification; // saved automatically when the transaction ends
    }
}
