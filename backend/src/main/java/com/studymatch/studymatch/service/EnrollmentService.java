package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.UnitEnrollment;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import com.studymatch.studymatch.repository.UnitEnrollmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

/**
 * Associates students (by number, for now) with curricular units.
 */
@Service
public class EnrollmentService {

    private final UnitEnrollmentRepository enrollmentRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final GroupConfigurationRepository groupConfigurationRepository;
    private final RolePreferenceRepository rolePreferenceRepository;
    private final NotificationService notificationService;

    /**
     * Receives the repositories and the notification service used by the enrollments.
     */
    public EnrollmentService(UnitEnrollmentRepository enrollmentRepository,
                             CurricularUnitRepository curricularUnitRepository,
                             GroupConfigurationRepository groupConfigurationRepository,
                             RolePreferenceRepository rolePreferenceRepository,
                             NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.curricularUnitRepository = curricularUnitRepository;
        this.groupConfigurationRepository = groupConfigurationRepository;
        this.rolePreferenceRepository = rolePreferenceRepository;
        this.notificationService = notificationService;
    }

    /**
     * Returns the student numbers of a curricular unit, or 404 if the unit does not exist.
     */
    @Transactional(readOnly = true)
    public List<String> getStudents(Long idUnit) {
        findUnit(idUnit);
        return enrollmentRepository.findByCurricularUnitIdUnitOrderByStudentNumber(idUnit).stream()
                .map(UnitEnrollment::getStudentNumber)
                .toList();
    }

    /**
     * Associates a student with a unit. If the unit already has an open group configuration,
     * the student is notified straight away.
     */
    @Transactional
    public String enrollStudent(Long idUnit, String studentNumber) {
        String number = StudentNumbers.validate(studentNumber);
        CurricularUnit unit = findUnit(idUnit);
        if (enrollmentRepository.existsByCurricularUnitIdUnitAndStudentNumber(idUnit, number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O aluno " + number + " já está associado à UC " + unit.getCodeUnit());
        }
        enrollmentRepository.save(new UnitEnrollment(unit, number));
        if (groupConfigurationRepository.existsByCurricularUnitIdUnit(idUnit)) {
            notificationService.notifyConfigurationOpened(unit, List.of(number));
        }
        return number;
    }

    /**
     * Removes a student from a unit, together with the role preference they submitted there.
     */
    @Transactional
    public void removeStudent(Long idUnit, String studentNumber) {
        String number = StudentNumbers.validate(studentNumber);
        CurricularUnit unit = findUnit(idUnit);
        UnitEnrollment enrollment = enrollmentRepository.findByCurricularUnitIdUnitAndStudentNumber(idUnit, number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "O aluno " + number + " não está associado à UC " + unit.getCodeUnit()));
        rolePreferenceRepository.deleteByGroupConfigurationCurricularUnitIdUnitAndStudentNumber(idUnit, number);
        enrollmentRepository.delete(enrollment);
    }

    /**
     * Returns true if the student is associated with the unit.
     */
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long idUnit, String studentNumber) {
        return enrollmentRepository.existsByCurricularUnitIdUnitAndStudentNumber(idUnit, studentNumber);
    }

    /**
     * Returns how many students the unit has.
     */
    @Transactional(readOnly = true)
    public long countStudents(Long idUnit) {
        return enrollmentRepository.countByCurricularUnitIdUnit(idUnit);
    }

    /**
     * Returns only the units of the student whose group configuration is open,
     * i.e. the ones where they can answer the questionnaire.
     */
    @Transactional(readOnly = true)
    public List<CurricularUnitResponse> getOpenUnits(String studentNumber) {
        String number = StudentNumbers.validate(studentNumber);
        return enrollmentRepository.findByStudentNumber(number).stream()
                .map(UnitEnrollment::getCurricularUnit)
                .filter(unit -> groupConfigurationRepository.existsByCurricularUnitIdUnit(unit.getIdUnit()))
                .sorted(Comparator.comparing(CurricularUnit::getCodeUnit))
                .map(CurricularUnitResponse::from)
                .toList();
    }

    private CurricularUnit findUnit(Long idUnit) {
        return curricularUnitRepository.findById(idUnit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + idUnit + " não encontrada"));
    }
}
