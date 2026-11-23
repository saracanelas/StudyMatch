package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="curricular_units")

public class CurricularUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnit;

    @NotBlank
    @Size(max = 12)
    @Column(nullable = false, unique = true, length = 12)
    private String codeUnit;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nameUnit;

    /** Number of ECTS credits given to a curricular unit when none is indicated. */
    public static final int DEFAULT_ECTS = 6;

    // ECTS credits: the weight of this unit in the students' grade average
    @NotNull
    @Min(1)
    @Max(60)
    @Column(nullable = false, columnDefinition = "integer default " + DEFAULT_ECTS)
    private Integer ects = DEFAULT_ECTS;

    // Ignored in JSON so that course -> units -> courses does not loop forever
    @JsonIgnore
    @ManyToMany(mappedBy = "curricularUnits")
    private List<Course> courses  = new ArrayList<>();



    /**
     * Empty constructor required by JPA.
     */
    public CurricularUnit() {}

    /**
     * Changes the unit code.
     */
    public void setCodeUnit(String codeUnit) {
        this.codeUnit = codeUnit;
    }

    /**
     * Changes the unit name.
     */
    public void setNameUnit(String nameUnit) {
        this.nameUnit = nameUnit;
    }

    /**
     * Creates a curricular unit with the given id, code and name.
     */
    public CurricularUnit(Long idUnit, String codeUnit, String nameUnit) {
        this.idUnit = idUnit;
        this.codeUnit = codeUnit;
        this.nameUnit = nameUnit;
    }

    /**
     * Returns the unit id.
     */
    public Long getIdUnit() {
        return idUnit;
    }

    /**
     * Returns the unit code.
     */
    public String getCodeUnit() {
        return codeUnit;
    }

    /**
     * Returns the unit name.
     */
    public String getNameUnit() {
        return nameUnit;
    }

    /**
     * Returns the courses this unit belongs to.
     */
    public List<Course> getCourses() {
        return courses;
    }

    /**
     * Returns the number of ECTS credits of the unit.
     */
    public Integer getEcts() {
        return ects;
    }

    /**
     * Changes the number of ECTS credits of the unit.
     */
    public void setEcts(Integer ects) {
        this.ects = ects;
    }
}
