package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Message shown to a student, for example when a group configuration opens in one of their units.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotification;

    // Temporary: there is no Student entity yet, so the student is identified by their number
    @Column(name = "student_number", nullable = false, length = 20)
    private String studentNumber;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unit", nullable = false)
    private CurricularUnit curricularUnit;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    /**
     * Empty constructor required by JPA.
     */
    public Notification() {}

    /**
     * Creates an unread notification for the student about the given unit.
     */
    public Notification(String studentNumber, CurricularUnit curricularUnit, String message) {
        this.studentNumber = studentNumber;
        this.curricularUnit = curricularUnit;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public Long getIdNotification() { return idNotification; }

    public String getStudentNumber() { return studentNumber; }

    /**
     * Returns the id of the curricular unit, so the JSON shows which unit this is about.
     */
    public Long getIdUnit() { return curricularUnit == null ? null : curricularUnit.getIdUnit(); }

    public String getMessage() { return message; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isRead() { return read; }

    /**
     * Marks the notification as read.
     */
    public void markAsRead() { this.read = true; }
}
