package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Group configurations are final: once the teacher submits one it cannot be changed. To change it,
 * the administrator deletes it (together with every student answer) and the teacher creates a new one.
 */
@Service
public class GroupConfigurationService {

    private final GroupConfigurationRepository groupConfigurationRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final RolePreferenceRepository rolePreferenceRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;

    /**
     * Receives the repositories and services used to read, save and delete group configurations.
     */
    public GroupConfigurationService(GroupConfigurationRepository groupConfigurationRepository,
                                     CurricularUnitRepository curricularUnitRepository,
                                     RolePreferenceRepository rolePreferenceRepository,
                                     EnrollmentService enrollmentService,
                                     NotificationService notificationService) {
        this.groupConfigurationRepository = groupConfigurationRepository;
        this.curricularUnitRepository = curricularUnitRepository;
        this.rolePreferenceRepository = rolePreferenceRepository;
        this.enrollmentService = enrollmentService;
        this.notificationService = notificationService;
    }

    /**
     * Returns the group configuration of a curricular unit, or 404 if the unit or its configuration does not exist.
     */
    @Transactional(readOnly = true)
    public GroupConfiguration getConfiguration(Long idUnit) {
        findUnit(idUnit);
        return groupConfigurationRepository.findByCurricularUnitIdUnit(idUnit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A UC " + idUnit + " ainda não tem configuração de grupos"));
    }

    /**
     * Creates the (final) group configuration of a curricular unit and notifies its students.
     * Refused if the unit already has one, or if the unit has fewer students than one group needs.
     */
    @Transactional
    public GroupConfiguration createConfiguration(Long idUnit, GroupConfigurationRequest request) {
        CurricularUnit unit = findUnit(idUnit);
        if (groupConfigurationRepository.existsByCurricularUnitIdUnit(idUnit)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A UC " + unit.getCodeUnit() + " já tem configuração de grupos. A configuração é final: "
                            + "para a alterar, peça ao administrador para a eliminar");
        }
        long studentsInUnit = enrollmentService.countStudents(idUnit);
        if (studentsInUnit < request.numberOfStudents()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A UC " + unit.getCodeUnit() + " tem " + studentsInUnit + " aluno(s) associado(s); "
                            + "não é possível formar grupos de " + request.numberOfStudents() + " alunos");
        }
        GroupConfiguration configuration = groupConfigurationRepository.save(
                new GroupConfiguration(unit, request.numberOfStudents(), request.trimmedRoles()));
        notificationService.notifyConfigurationOpened(unit, enrollmentService.getStudents(idUnit));
        return configuration;
    }

    /**
     * Administrator only: deletes the configuration of a unit and every role preference submitted for it,
     * so the teacher can create a new one.
     */
    @Transactional
    public void deleteConfiguration(Long idUnit) {
        GroupConfiguration configuration = getConfiguration(idUnit);
        rolePreferenceRepository.deleteByGroupConfigurationIdConfiguration(configuration.getIdConfiguration());
        groupConfigurationRepository.delete(configuration);
    }

    private CurricularUnit findUnit(Long idUnit) {
        return curricularUnitRepository.findById(idUnit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + idUnit + " não encontrada"));
    }
}
