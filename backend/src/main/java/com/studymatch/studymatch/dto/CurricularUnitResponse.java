package com.studymatch.studymatch.dto;

import com.studymatch.studymatch.model.CurricularUnit;

/**
 * Curricular unit data returned to the client.
 */
public record CurricularUnitResponse(Long idUnit, String codeUnit, String nameUnit) {

    /**
     * Builds the response from a curricular unit entity.
     */
    public static CurricularUnitResponse from(CurricularUnit unit) {
        return new CurricularUnitResponse(unit.getIdUnit(), unit.getCodeUnit(), unit.getNameUnit());
    }
}
