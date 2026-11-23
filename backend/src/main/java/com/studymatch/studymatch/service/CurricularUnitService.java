package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.CurricularUnitRequest;
import com.studymatch.studymatch.dto.CurricularUnitResponse;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CurricularUnitService {

    private final CurricularUnitRepository curricularUnitRepository;
    private final GradeAverageService gradeAverageService;

    /**
     * Receives the repository used to read and save curricular units and the service that
     * recalculates the students' averages when the ECTS of a unit change.
     */
    public CurricularUnitService(CurricularUnitRepository curricularUnitRepository, GradeAverageService gradeAverageService) {
        this.curricularUnitRepository = curricularUnitRepository;
        this.gradeAverageService = gradeAverageService;
    }

    /**
     * Returns every curricular unit in the database.
     */
    @Transactional(readOnly = true)
    public List<CurricularUnitResponse> getAllCurricularUnits() {
        return curricularUnitRepository.findAll().stream().map(CurricularUnitResponse::from).toList();
    }

    /**
     * Saves a new curricular unit, refusing it if the code is already in use.
     */
    @Transactional
    public CurricularUnitResponse createCurricularUnit(CurricularUnitRequest newUnit) {
        if (curricularUnitRepository.findByCodeUnit(newUnit.codeUnit()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma UC com o código " + newUnit.codeUnit());
        }
        CurricularUnit unit = new CurricularUnit(null, newUnit.codeUnit(), newUnit.nameUnit());
        unit.setEcts(newUnit.ects() != null ? newUnit.ects() : CurricularUnit.DEFAULT_ECTS);
        return CurricularUnitResponse.from(curricularUnitRepository.save(unit));
    }

    /**
     * Changes the code, name and ECTS of an existing curricular unit, keeping codes unique.
     */
    @Transactional
    public CurricularUnitResponse updateCurricularUnit(Long id, CurricularUnitRequest updatedUnit) {
        CurricularUnit unit = curricularUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + id + " não encontrada"));
        CurricularUnit unitWithSameCode = curricularUnitRepository.findByCodeUnit(updatedUnit.codeUnit());
        if (unitWithSameCode != null && !unitWithSameCode.getIdUnit().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma UC com o código " + updatedUnit.codeUnit());
        }
        unit.setCodeUnit(updatedUnit.codeUnit());
        unit.setNameUnit(updatedUnit.nameUnit());
        if (updatedUnit.ects() != null && !updatedUnit.ects().equals(unit.getEcts())) {
            unit.setEcts(updatedUnit.ects());
            // the ECTS are the weights of the grade averages, so the enrolled students must be updated
            gradeAverageService.recalculateForUnit(id);
        }
        return CurricularUnitResponse.from(unit); // saved automatically when the transaction ends
    }
}
