package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.UnitEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitEnrollmentRepository extends JpaRepository<UnitEnrollment, Long> {

    /**
     * Returns the students of a curricular unit, ordered by student number.
     */
    List<UnitEnrollment> findByCurricularUnitIdUnitOrderByStudentNumber(Long idUnit);

    /**
     * Returns the units a student is associated with.
     */
    List<UnitEnrollment> findByStudentNumber(String studentNumber);

    /**
     * Returns the association between a unit and a student, if any.
     */
    Optional<UnitEnrollment> findByCurricularUnitIdUnitAndStudentNumber(Long idUnit, String studentNumber);

    /**
     * Returns true if the student is associated with the unit.
     */
    boolean existsByCurricularUnitIdUnitAndStudentNumber(Long idUnit, String studentNumber);

    /**
     * Returns how many students the unit has.
     */
    long countByCurricularUnitIdUnit(Long idUnit);
}
