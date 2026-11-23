package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.model.RolePreference;
import com.studymatch.studymatch.model.UnitEnrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GroupRepositoriesTest {

    @Autowired private CurricularUnitRepository unitRepository;
    @Autowired private GroupConfigurationRepository configurationRepository;
    @Autowired private RolePreferenceRepository preferenceRepository;
    @Autowired private UnitEnrollmentRepository enrollmentRepository;
    @Autowired private TestEntityManager em;

    private CurricularUnit unit;
    private GroupConfiguration configuration;

    @BeforeEach
    void setUp() {
        unit = unitRepository.save(new CurricularUnit(null, "SQ", "Software Quality"));
        configuration = configurationRepository.save(new GroupConfiguration(unit, 2, List.of("A", "B")));
        preferenceRepository.save(new RolePreference(configuration, "111", List.of("A", "B")));
        preferenceRepository.save(new RolePreference(configuration, "222", List.of("B", "A")));
        em.flush();
    }

    @Test
    @DisplayName("deleteByGroupConfigurationIdConfiguration removes every answer, so the configuration can be deleted")
    void deleteAllAnswersThenConfiguration() {
        preferenceRepository.deleteByGroupConfigurationIdConfiguration(configuration.getIdConfiguration());
        configurationRepository.delete(configuration);
        em.flush();

        assertEquals(0, preferenceRepository.count());
        assertFalse(configurationRepository.existsByCurricularUnitIdUnit(unit.getIdUnit()));
    }

    @Test
    @DisplayName("deleteByGroupConfigurationCurricularUnitIdUnitAndStudentNumber removes only that student's answer")
    void deleteOneStudentAnswer() {
        preferenceRepository.deleteByGroupConfigurationCurricularUnitIdUnitAndStudentNumber(unit.getIdUnit(), "111");
        em.flush();

        assertEquals(1, preferenceRepository.count());
        assertTrue(preferenceRepository.findByGroupConfigurationIdConfigurationAndStudentNumber(
                configuration.getIdConfiguration(), "222").isPresent());
    }

    @Test
    @DisplayName("enrollments are counted per unit and a student cannot be enrolled twice")
    void enrollments() {
        enrollmentRepository.save(new UnitEnrollment(unit, "111"));
        enrollmentRepository.save(new UnitEnrollment(unit, "222"));
        em.flush();

        assertEquals(2, enrollmentRepository.countByCurricularUnitIdUnit(unit.getIdUnit()));
        assertTrue(enrollmentRepository.existsByCurricularUnitIdUnitAndStudentNumber(unit.getIdUnit(), "111"));
        assertThrows(DataIntegrityViolationException.class,
                () -> enrollmentRepository.saveAndFlush(new UnitEnrollment(unit, "111")));
    }
}
