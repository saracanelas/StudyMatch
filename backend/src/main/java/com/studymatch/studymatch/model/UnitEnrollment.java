package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * A student associated with a curricular unit.
 * Temporary: there is no Student entity yet, so the student is identified by their number.
 */
@Entity
@Table(name = "unit_enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_unit", "student_number"}))
public class UnitEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEnrollment;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unit", nullable = false)
    private CurricularUnit curricularUnit;

    @Column(name = "student_number", nullable = false, length = 20)
    private String studentNumber;

    /**
     * Empty constructor required by JPA.
     */
    public UnitEnrollment() {}

    /**
     * Associates the student with the given unit.
     */
    public UnitEnrollment(CurricularUnit curricularUnit, String studentNumber) {
        this.curricularUnit = curricularUnit;
        this.studentNumber = studentNumber;
    }

    public Long getIdEnrollment() { return idEnrollment; }

    public CurricularUnit getCurricularUnit() { return curricularUnit; }

    public String getStudentNumber() { return studentNumber; }
}
