package com.studymatch.studymatch.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="curricular_units")

public class CurricularUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnit;

    @Column(nullable = false, unique = true, length = 12)
    private String codeUnit;

    @Column(nullable = false, length = 50)
    private String nameUnit;

    @ManyToMany(mappedBy = "curricularUnits")
    private List<Course> courses  = new ArrayList<>();



    public CurricularUnit() {}

    public void setCodeUnit(String codeUnit) {
        this.codeUnit = codeUnit;
    }

    public void setNameUnit(String nameUnit) {
        this.nameUnit = nameUnit;
    }

    public CurricularUnit(Long idUnit, String codeUnit, String nameUnit) {
        this.idUnit = idUnit;
        this.codeUnit = codeUnit;
        this.nameUnit = nameUnit;
    }

    public Long getIdUnit() {
        return idUnit;
    }

    public String getCodeUnit() {
        return codeUnit;
    }

    public String getNameUnit() {
        return nameUnit;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
