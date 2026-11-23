package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.Notification;
import com.studymatch.studymatch.service.EnrollmentService;
import com.studymatch.studymatch.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * What a student can see: their open units and their notifications.
 * Temporary: the student is identified by the number in the URL until students can log in.
 */
@RestController
@RequestMapping("/api/students/{studentNumber}")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;

    /**
     * Receives the services used to find the units and notifications of a student.
     */
    public StudentController(EnrollmentService enrollmentService, NotificationService notificationService) {
        this.enrollmentService = enrollmentService;
        this.notificationService = notificationService;
    }

    /**
     * GET /api/students/{studentNumber}/units - units of the student whose group configuration is open.
     */
    @GetMapping("/units")
    public List<CurricularUnitResponse> getOpenUnits(@PathVariable String studentNumber) {
        return enrollmentService.getOpenUnits(studentNumber);
    }

    /**
     * GET /api/students/{studentNumber}/notifications - notifications of the student, newest first.
     */
    @GetMapping("/notifications")
    public List<Notification> getNotifications(@PathVariable String studentNumber) {
        return notificationService.getNotifications(studentNumber);
    }

    /**
     * PATCH /api/students/{studentNumber}/notifications/{id}/read - marks a notification as read, or 404.
     */
    @PatchMapping("/notifications/{idNotification}/read")
    public Notification markAsRead(@PathVariable String studentNumber, @PathVariable Long idNotification) {
        return notificationService.markAsRead(studentNumber, idNotification);
    }
}
