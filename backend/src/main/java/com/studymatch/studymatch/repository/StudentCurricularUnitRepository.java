package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.StudentCurricularUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentCurricularUnitRepository extends JpaRepository<StudentCurricularUnit, Long> {

    /**
     * Returns the enrollments of the student with the given id.
     */
    List<StudentCurricularUnit> findByStudent_IdStudent(Long idStudent);

    /**
     * Returns the enrollments in the curricular unit with the given id.
     */
    List<StudentCurricularUnit> findByCurricularUnit_IdUnit(Long idUnit);

    /**
     * Returns true if the student is already enrolled in the curricular unit.
     */
    boolean existsByStudent_IdStudentAndCurricularUnit_IdUnit(Long idStudent, Long idUnit);
}
