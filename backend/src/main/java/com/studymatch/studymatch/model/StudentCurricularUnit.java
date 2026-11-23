package com.studymatch.studymatch.model;

import jakarta.persistence.*;

/**
 * Enrollment of a student in a curricular unit, with the grade obtained (entity Student_CU of the UML).
 * A student can be enrolled only once in each curricular unit; the grade stays empty until it is registered.
 */
@Entity
@Table(name = "student_cu",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_cu", columnNames = {"idStudent", "idUnit"}))
public class StudentCurricularUnit {

    /** Lowest grade accepted. */
    public static final double MIN_GRADE = 0.0;
    /** Highest grade accepted. */
    public static final double MAX_GRADE = 20.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEnrollment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idStudent", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUnit", nullable = false)
    private CurricularUnit curricularUnit;

    // Grade obtained in the unit, from 0 to 20; null while it has not been registered
    private Double cuGrade;


    /**
     * Empty constructor required by JPA.
     */
    public StudentCurricularUnit() {}

    /**
     * Enrolls the given student in the given curricular unit, without a grade.
     */
    public StudentCurricularUnit(Student student, CurricularUnit curricularUnit) {
        this.student = student;
        this.curricularUnit = curricularUnit;
    }


    /**
     * Returns true if the grade is between 0 and 20 (inclusive).
     */
    public static boolean isValidGrade(double grade) {
        return grade >= MIN_GRADE && grade <= MAX_GRADE;
    }

    /**
     * Returns the enrollment id.
     */
    public Long getIdEnrollment() {
        return idEnrollment;
    }

    /**
     * Returns the enrolled student.
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Returns the curricular unit.
     */
    public CurricularUnit getCurricularUnit() {
        return curricularUnit;
    }

    /**
     * Returns the grade obtained, or null if it was not registered yet.
     */
    public Double getCuGrade() {
        return cuGrade;
    }

    /**
     * Registers or changes the grade (null clears it).
     */
    public void setCuGrade(Double cuGrade) {
        this.cuGrade = cuGrade;
    }
}
