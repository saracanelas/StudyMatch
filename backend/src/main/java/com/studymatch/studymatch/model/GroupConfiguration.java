package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Group setup of a curricular unit: how many students each group has and the role of each member.
 * The list of roles has one entry per member, so its size always equals the number of students.
 * It is final once created: there are no setters, it can only be deleted by the administrator.
 */
@Entity
@Table(name = "group_configurations")
public class GroupConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConfiguration;

    // One configuration per curricular unit
    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "id_unit", nullable = false, unique = true)
    private CurricularUnit curricularUnit;

    @Column(nullable = false)
    private int numberOfStudents;

    // Stored in table group_configuration_roles, keeping the order of the members
    @ElementCollection
    @CollectionTable(name = "group_configuration_roles",
            joinColumns = @JoinColumn(name = "id_configuration"))
    @OrderColumn(name = "member_position")
    @Column(name = "role_name", nullable = false, length = 50)
    private List<String> roles = new ArrayList<>();

    /**
     * Empty constructor required by JPA.
     */
    public GroupConfiguration() {}

    /**
     * Creates a configuration for the given unit with the number of students and the role of each member.
     */
    public GroupConfiguration(CurricularUnit curricularUnit, int numberOfStudents, List<String> roles) {
        this.curricularUnit = curricularUnit;
        this.numberOfStudents = numberOfStudents;
        this.roles = new ArrayList<>(roles);
    }

    public Long getIdConfiguration() { return idConfiguration; }

    public CurricularUnit getCurricularUnit() { return curricularUnit; }

    /**
     * Returns the id of the curricular unit, so the JSON shows which unit this belongs to.
     */
    public Long getIdUnit() { return curricularUnit == null ? null : curricularUnit.getIdUnit(); }

    public int getNumberOfStudents() { return numberOfStudents; }

    public List<String> getRoles() { return roles; }
}
