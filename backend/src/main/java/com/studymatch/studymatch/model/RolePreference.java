package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ordered list of roles a student would like to take in the groups of one assignment
 * (the group configuration of a curricular unit). Position 0 is the favourite role.
 * Later this will be used to form the groups.
 */
@Entity
@Table(name = "role_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_configuration", "student_number"}))
public class RolePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPreference;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_configuration", nullable = false)
    private GroupConfiguration groupConfiguration;

    // Temporary: there is no Student entity yet, so the student is identified by their number
    @Column(name = "student_number", nullable = false, length = 20)
    private String studentNumber;

    // Stored in table role_preference_rankings; rank_position 0 = first choice
    @ElementCollection
    @CollectionTable(name = "role_preference_rankings",
            joinColumns = @JoinColumn(name = "id_preference"))
    @OrderColumn(name = "rank_position")
    @Column(name = "role_name", nullable = false, length = 50)
    private List<String> rankedRoles = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Empty constructor required by JPA.
     */
    public RolePreference() {}

    /**
     * Creates the preference of a student for the given assignment.
     */
    public RolePreference(GroupConfiguration groupConfiguration, String studentNumber, List<String> rankedRoles) {
        this.groupConfiguration = groupConfiguration;
        this.studentNumber = studentNumber;
        this.rankedRoles = new ArrayList<>(rankedRoles);
        this.updatedAt = LocalDateTime.now();
    }

    public Long getIdPreference() { return idPreference; }

    public GroupConfiguration getGroupConfiguration() { return groupConfiguration; }

    /**
     * Returns the id of the curricular unit, so the JSON shows which assignment this belongs to.
     */
    public Long getIdUnit() { return groupConfiguration == null ? null : groupConfiguration.getIdUnit(); }

    public String getStudentNumber() { return studentNumber; }

    public List<String> getRankedRoles() { return rankedRoles; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Replaces the order of the roles.
     */
    public void updateRanking(List<String> rankedRoles) {
        this.rankedRoles.clear();
        this.rankedRoles.addAll(rankedRoles);
        this.updatedAt = LocalDateTime.now();
    }
}
