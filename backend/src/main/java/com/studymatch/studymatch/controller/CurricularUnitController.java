package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.service.CurricularUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
public class CurricularUnitController {

    private final CurricularUnitService curricularUnitService;

    /**
     * Receives the service that holds the curricular unit logic.
     */
    public CurricularUnitController(CurricularUnitService curricularUnitService) {
        this.curricularUnitService = curricularUnitService;
    }

    /**
     * GET /api/units - lists all curricular units.
     */
    @GetMapping
    public List<CurricularUnit> getAllCurricularUnits() {
        return curricularUnitService.getAllCurricularUnits();
    }

    /**
     * POST /api/units - creates a curricular unit from its code and name.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurricularUnit createCurricularUnit(@Valid @RequestBody CurricularUnit newUnit) {
        return curricularUnitService.createCurricularUnit(newUnit);
    }

    /**
     * PUT /api/units/{id} - changes the code and name of a curricular unit.
     */
    @PutMapping("/{id}")
    public CurricularUnit updateCurricularUnit(@PathVariable Long id, @Valid @RequestBody CurricularUnit updatedUnit) {
        return curricularUnitService.updateCurricularUnit(id, updatedUnit);
    }
}
