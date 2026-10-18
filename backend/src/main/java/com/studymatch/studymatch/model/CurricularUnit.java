package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
}
