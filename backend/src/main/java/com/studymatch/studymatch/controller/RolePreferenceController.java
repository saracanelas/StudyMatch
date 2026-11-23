package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.RolePreferenceRequest;
import com.studymatch.studymatch.model.RolePreference;
import com.studymatch.studymatch.service.RolePreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role preferences of the students for the assignment (group configuration) of a curricular unit.
 * Errors (400 / 404) are turned into { "error": message } by {@link ApiErrorHandler}.
 */
@RestController
@RequestMapping("/api/units/{idUnit}")
public class RolePreferenceController {

    private final RolePreferenceService rolePreferenceService;

    /**
     * Receives the service that holds the role preference logic.
     */
    public RolePreferenceController(RolePreferenceService rolePreferenceService) {
        this.rolePreferenceService = rolePreferenceService;
    }

    /**
     * GET /api/units/{idUnit}/roles - lists the roles of the assignment, or 404 if it has no configuration.
     */
    @GetMapping("/roles")
    public List<String> getAvailableRoles(@PathVariable Long idUnit) {
        return rolePreferenceService.getAvailableRoles(idUnit);
    }

    /**
     * GET /api/units/{idUnit}/role-preferences/{studentNumber} - returns the student's ranking, or 404.
     */
    @GetMapping("/role-preferences/{studentNumber}")
    public RolePreference getPreference(@PathVariable Long idUnit, @PathVariable String studentNumber) {
        return rolePreferenceService.getPreference(idUnit, studentNumber);
    }

    /**
     * PUT /api/units/{idUnit}/role-preferences/{studentNumber} - submits (201) or updates (200) the ranking.
     */
    @PutMapping("/role-preferences/{studentNumber}")
    public ResponseEntity<RolePreference> submitPreference(@PathVariable Long idUnit,
                                                           @PathVariable String studentNumber,
                                                           @Valid @RequestBody RolePreferenceRequest request) {
        RolePreferenceService.SubmitResult result =
                rolePreferenceService.submitPreference(idUnit, studentNumber, request.trimmedRoles());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.preference());
    }
}
