package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import com.studymatch.studymatch.repository.GroupConfigurationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GroupConfigurationService {

    private final GroupConfigurationRepository groupConfigurationRepository;
    private final CurricularUnitRepository curricularUnitRepository;

    /**
     * Receives the repositories used to read and save group configurations and curricular units.
     */
    public GroupConfigurationService(GroupConfigurationRepository groupConfigurationRepository,
                                     CurricularUnitRepository curricularUnitRepository) {
        this.groupConfigurationRepository = groupConfigurationRepository;
        this.curricularUnitRepository = curricularUnitRepository;
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
     * Creates the group configuration of a curricular unit, refusing it if the unit already has one.
     */
    @Transactional
    public GroupConfiguration createConfiguration(Long idUnit, GroupConfigurationRequest request) {
        CurricularUnit unit = findUnit(idUnit);
        if (groupConfigurationRepository.findByCurricularUnitIdUnit(idUnit).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A UC " + unit.getCodeUnit() + " já tem configuração de grupos");
        }
        GroupConfiguration configuration =
                new GroupConfiguration(unit, request.numberOfStudents(), request.trimmedRoles());
        return groupConfigurationRepository.save(configuration);
    }

    /**
     * Changes the number of students and roles of the existing configuration of a curricular unit.
     */
    @Transactional
    public GroupConfiguration updateConfiguration(Long idUnit, GroupConfigurationRequest request) {
        GroupConfiguration configuration = getConfiguration(idUnit);
        configuration.update(request.numberOfStudents(), request.trimmedRoles());
        return configuration; // saved automatically when the transaction ends
    }

    private CurricularUnit findUnit(Long idUnit) {
        return curricularUnitRepository.findById(idUnit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + idUnit + " não encontrada"));
    }
}
