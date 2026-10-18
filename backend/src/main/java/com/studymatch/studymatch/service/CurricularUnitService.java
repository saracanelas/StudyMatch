package com.studymatch.studymatch.service;

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
    public List<CurricularUnit> getAllCurricularUnits() {
        return curricularUnitRepository.findAll();
    }

    /**
     * Saves a new curricular unit, refusing it if the code is already in use.
     */
    @Transactional
    public CurricularUnit createCurricularUnit(CurricularUnit newUnit) {
        if (curricularUnitRepository.findByCodeUnit(newUnit.getCodeUnit()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma UC com o código " + newUnit.getCodeUnit());
        }
        CurricularUnit unit = new CurricularUnit(null, newUnit.getCodeUnit(), newUnit.getNameUnit());
        return curricularUnitRepository.save(unit);
    }

    /**
     * Changes the code and name of an existing curricular unit, keeping codes unique.
     */
    @Transactional
    public CurricularUnit updateCurricularUnit(Long id, CurricularUnit updatedUnit) {
        CurricularUnit unit = curricularUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UC " + id + " não encontrada"));
        CurricularUnit unitWithSameCode = curricularUnitRepository.findByCodeUnit(updatedUnit.getCodeUnit());
        if (unitWithSameCode != null && !unitWithSameCode.getIdUnit().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma UC com o código " + updatedUnit.getCodeUnit());
        }
        unit.setCodeUnit(updatedUnit.getCodeUnit());
        unit.setNameUnit(updatedUnit.getNameUnit());
        return unit; // saved automatically when the transaction ends
    }
}
