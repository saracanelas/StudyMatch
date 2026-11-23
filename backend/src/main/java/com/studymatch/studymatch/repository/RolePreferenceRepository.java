package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.RolePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolePreferenceRepository extends JpaRepository<RolePreference, Long> {

    /**
     * Returns the preference a student submitted for the given group configuration, if any.
     */
    Optional<RolePreference> findByGroupConfigurationIdConfigurationAndStudentNumber(Long idConfiguration,
                                                                                    String studentNumber);

    /**
     * Deletes every preference submitted for the given group configuration.
     */
    void deleteByGroupConfigurationIdConfiguration(Long idConfiguration);

    /**
     * Deletes the preference a student submitted in the given unit, if any.
     */
    void deleteByGroupConfigurationCurricularUnitIdUnitAndStudentNumber(Long idUnit, String studentNumber);
}
