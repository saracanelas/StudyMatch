package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Returns the notifications of a student, newest first.
     */
    List<Notification> findByStudentNumberOrderByCreatedAtDescIdNotificationDesc(String studentNumber);

    /**
     * Returns the notification with the given id if it belongs to the student.
     */
    Optional<Notification> findByIdNotificationAndStudentNumber(Long idNotification, String studentNumber);
}
