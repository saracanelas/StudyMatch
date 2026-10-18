package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.service.CurricularUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

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
     * POST /api/units - creates a curricular unit from its code and name, or returns 409 if the code is taken.
     */
    @PostMapping
    public ResponseEntity<?> createCurricularUnit(@Valid @RequestBody CurricularUnit newUnit) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(curricularUnitService.createCurricularUnit(newUnit));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * PUT /api/units/{id} - changes the code and name of a curricular unit, or returns 404 / 409 with an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurricularUnit(@PathVariable Long id, @Valid @RequestBody CurricularUnit updatedUnit) {
        try {
            return ResponseEntity.ok(curricularUnitService.updateCurricularUnit(id, updatedUnit));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    /**
     * Turns an error thrown by the service into a response with its status code and { "error": message }.
     */
    private ResponseEntity<Map<String, String>> errorResponse(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
    }
}
