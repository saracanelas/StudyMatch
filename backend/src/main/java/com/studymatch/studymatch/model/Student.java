package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * A student registered in StudyMatch (entity Student of the UML).
 * Each student has a unique student number and e-mail, the year in which they enrolled
 * and, optionally, a birthday, a class and the course they attend.
 * The grade average is calculated by the server from the grades of the enrolled curricular units.
 */
@Entity
@Table(name = "students")
public class Student {

    /** Lowest enrollment year accepted. */
    public static final int MIN_ENROLLMENT_YEAR = 1950;
    /** Highest enrollment year accepted. */
    public static final int MAX_ENROLLMENT_YEAR = 2100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStudent;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String numberStudent;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nameStudent;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String emailStudent;

    @Past
    private LocalDate birthday;

    @NotNull
    @Min(MIN_ENROLLMENT_YEAR)
    @Max(MAX_ENROLLMENT_YEAR)
    @Column(nullable = false)
    private Integer enrollmentYear;

    // Class ("turma") of the student, e.g. "A" or "3LEI-T1"
    @Size(max = 20)
    @Column(length = 20)
    private String classStudent;

    // Course the student attends; null while the student is not placed in a course.
    // Its curricular units are left out of the JSON to keep student responses small.
    @ManyToOne
    @JoinColumn(name = "idCourse")
    @JsonIgnoreProperties("curricularUnits")
    private Course course;

    // Average of the graded curricular units weighted by their ECTS; null while there are no grades.
    // Calculated by the server, so it is ignored when sent by a client.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double gradeAverage;


    /**
     * Empty constructor required by JPA.
     */
    public Student() {}

    /**
     * Creates a student with the given id, number, name, e-mail and enrollment year.
     */
    public Student(Long idStudent, String numberStudent, String nameStudent, String emailStudent, Integer enrollmentYear) {
        this.idStudent = idStudent;
        this.numberStudent = numberStudent;
        this.nameStudent = nameStudent;
        this.emailStudent = emailStudent;
        this.enrollmentYear = enrollmentYear;
    }


    /**
     * Returns the student id.
     */
    public Long getIdStudent() {
        return idStudent;
    }

    /**
     * Returns the student number.
     */
    public String getNumberStudent() {
        return numberStudent;
    }

    /**
     * Returns the student name.
     */
    public String getNameStudent() {
        return nameStudent;
    }

    /**
     * Returns the student e-mail.
     */
    public String getEmailStudent() {
        return emailStudent;
    }

    /**
     * Returns the student birthday, or null if unknown.
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * Returns the year in which the student enrolled.
     */
    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    /**
     * Returns the class of the student, or null if none.
     */
    public String getClassStudent() {
        return classStudent;
    }

    /**
     * Returns the course the student attends, or null if none.
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Returns the ECTS-weighted grade average, or null while the student has no grades.
     */
    public Double getGradeAverage() {
        return gradeAverage;
    }

    /**
     * Changes the student number.
     */
    public void setNumberStudent(String numberStudent) {
        this.numberStudent = numberStudent;
    }

    /**
     * Changes the student name.
     */
    public void setNameStudent(String nameStudent) {
        this.nameStudent = nameStudent;
    }

    /**
     * Changes the student e-mail.
     */
    public void setEmailStudent(String emailStudent) {
        this.emailStudent = emailStudent;
    }

    /**
     * Changes the student birthday.
     */
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    /**
     * Changes the enrollment year.
     */
    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    /**
     * Changes the class of the student.
     */
    public void setClassStudent(String classStudent) {
        this.classStudent = classStudent;
    }

    /**
     * Changes the course the student attends (null removes the student from any course).
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * Changes the grade average; used by the server when grades change.
     */
    public void setGradeAverage(Double gradeAverage) {
        this.gradeAverage = gradeAverage;
    }
}
