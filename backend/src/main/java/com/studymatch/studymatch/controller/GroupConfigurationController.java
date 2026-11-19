package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.GroupConfigurationRequest;
import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.service.GroupConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Errors (400 / 404 / 409) are turned into { "error": message } by {@link ApiErrorHandler}.
 */
@RestController
@RequestMapping("/api/units/{idUnit}/group-configuration")
public class GroupConfigurationController {

    private final GroupConfigurationService groupConfigurationService;

    /**
     * Receives the service that holds the group configuration logic.
     */
    public GroupConfigurationController(GroupConfigurationService groupConfigurationService) {
        this.groupConfigurationService = groupConfigurationService;
    }

    /**
     * GET /api/units/{idUnit}/group-configuration - returns the configuration of the unit, or 404.
     */
    @GetMapping
    public GroupConfiguration getConfiguration(@PathVariable Long idUnit) {
        return groupConfigurationService.getConfiguration(idUnit);
    }

    /**
     * POST /api/units/{idUnit}/group-configuration - creates the configuration, or returns 404 / 409.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupConfiguration createConfiguration(@PathVariable Long idUnit,
                                                  @Valid @RequestBody GroupConfigurationRequest request) {
        return groupConfigurationService.createConfiguration(idUnit, request);
    }

    /**
     * PUT /api/units/{idUnit}/group-configuration - changes the existing configuration, or returns 404.
     */
    @PutMapping
    public GroupConfiguration updateConfiguration(@PathVariable Long idUnit,
                                                  @Valid @RequestBody GroupConfigurationRequest request) {
        return groupConfigurationService.updateConfiguration(idUnit, request);
    }
}
