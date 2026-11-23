package com.studymatch.studymatch.dto;

import com.studymatch.studymatch.model.CurricularUnit;

/**
 * Curricular unit data returned to the client.
 */
public record CurricularUnitResponse(Long idUnit, String codeUnit, String nameUnit, Integer ects) {

    /**
     * Response of a unit with the default number of ECTS.
     */
    public CurricularUnitResponse(Long idUnit, String codeUnit, String nameUnit) {
        this(idUnit, codeUnit, nameUnit, CurricularUnit.DEFAULT_ECTS);
    }

    /**
     * Builds the response from a curricular unit entity.
     */
    public static CurricularUnitResponse from(CurricularUnit unit) {
        return new CurricularUnitResponse(unit.getIdUnit(), unit.getCodeUnit(), unit.getNameUnit(), unit.getEcts());
    }
}
