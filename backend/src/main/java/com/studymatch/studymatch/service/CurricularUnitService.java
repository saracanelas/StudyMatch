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

    /**
     * Receives the repository used to read and save curricular units.
     */
    public CurricularUnitService(CurricularUnitRepository curricularUnitRepository) {
        this.curricularUnitRepository = curricularUnitRepository;
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
        return CurricularUnitResponse.from(curricularUnitRepository.save(unit));
    }

    /**
     * Changes the code and name of an existing curricular unit, keeping codes unique.
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
        return CurricularUnitResponse.from(unit); // saved automatically when the transaction ends
    }
}
