package com.studymatch.studymatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "courses")

public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCourse;

    @NotBlank
    @Size(max = 12)
    @Column(nullable = false, unique = true, length = 12)
    private String codeCourse;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nameCourse;

    @ManyToMany
    @JoinTable(
            name="course_unit",
            joinColumns = @JoinColumn(name="idCourse"),
            inverseJoinColumns = @JoinColumn(name="idUnit")
    )
    private List<CurricularUnit> curricularUnits = new ArrayList<>();


    /**
     * Empty constructor required by JPA.
     */
    public Course() {}

    /**
     * Creates a course with the given id, code and name.
     */
    public Course(Long idCourse, String codeCourse, String nameCourse) {
        this.idCourse = idCourse;
        this.codeCourse = codeCourse;
        this.nameCourse = nameCourse;
    }


    /**
     * Returns the course id.
     */
    public Long getIdCourse() {
        return idCourse;
    }

    /**
     * Returns the course code.
     */
    public String getCodeCourse() {
        return codeCourse;
    }

    /**
     * Returns the course name.
     */
    public String getNameCourse() {
        return nameCourse;
    }

    /**
     * Returns the curricular units associated with this course.
     */
    public List<CurricularUnit> getCurricularUnits() {
        return curricularUnits;
    }

    /**
     * Changes the course code.
     */
    public void setCodeCourse(String codeCourse) {
        this.codeCourse = codeCourse;
    }

    /**
     * Changes the course name.
     */
    public void setNameCourse(String nameCourse) {
        this.nameCourse = nameCourse;
    }

    /**
     * These next two methods are used to guarantee consistency on both sides, since curricular units will always
     * be added and removed in association to the existing courses
     * @param unit
     */

    public void addCurricularUnit(CurricularUnit unit) {
        this.curricularUnits.add(unit);
        unit.getCourses().add(this);
    }

    /**
     * Removes a curricular unit from this course, on both sides of the relation.
     */
    public void removeCurricularUnit(CurricularUnit unit) {
        this.curricularUnits.remove(unit);
        unit.getCourses().remove(this);
    }
}
