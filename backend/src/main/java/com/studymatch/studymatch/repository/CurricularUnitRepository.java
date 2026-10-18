package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.CurricularUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CurricularUnitRepository extends JpaRepository<CurricularUnit, Long> {

    /**
     * Returns the curricular unit with exactly this code, or null if there is none.
     */
    CurricularUnit findByCodeUnit(String codeUnit);

    /**
     * Returns the curricular units whose name contains the given text, ignoring case.
     */
    List<CurricularUnit> findByNameUnitContainingIgnoreCase(String name);

}
