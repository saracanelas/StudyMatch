package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.GroupConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupConfigurationRepository extends JpaRepository<GroupConfiguration, Long> {

    /**
     * Returns the group configuration of the given curricular unit, if it has one.
     */
    Optional<GroupConfiguration> findByCurricularUnitIdUnit(Long idUnit);

    /**
     * Returns true if the curricular unit has a group configuration (i.e. it is open to students).
     */
    boolean existsByCurricularUnitIdUnit(Long idUnit);
}
