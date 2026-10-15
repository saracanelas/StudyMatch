package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.CurricularUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CurricularUnitRepository extends JpaRepository<CurricularUnit, Long> {

    CurricularUnit findByCurricularUnitCode(String curricularUnitCode);

    List<CurricularUnit> findByNameUnitContainingIgnoreCase(String name);

}
