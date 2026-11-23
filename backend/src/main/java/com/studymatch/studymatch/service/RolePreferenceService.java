package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.GroupConfiguration;
import com.studymatch.studymatch.model.RolePreference;
import com.studymatch.studymatch.repository.RolePreferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RolePreferenceService {

    private final RolePreferenceRepository rolePreferenceRepository;
    private final GroupConfigurationService groupConfigurationService;
    private final EnrollmentService enrollmentService;

    /**
     * Receives the repository of preferences and the services used to find the group configuration
     * and to check that the student belongs to the unit.
     */
    public RolePreferenceService(RolePreferenceRepository rolePreferenceRepository,
                                 GroupConfigurationService groupConfigurationService,
                                 EnrollmentService enrollmentService) {
        this.rolePreferenceRepository = rolePreferenceRepository;
        this.groupConfigurationService = groupConfigurationService;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Returns the roles a student can rank in the assignment of a unit: each role of the
     * configuration once, in the order the teacher wrote them.
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableRoles(Long idUnit) {
        return availableRoles(groupConfigurationService.getConfiguration(idUnit));
    }

    /**
     * Returns the preference a student already submitted for the unit, 404 if there is none,
     * or 403 if the student does not belong to the unit.
     */
    @Transactional(readOnly = true)
    public RolePreference getPreference(Long idUnit, String studentNumber) {
        checkEnrolled(idUnit, studentNumber);
        GroupConfiguration configuration = groupConfigurationService.getConfiguration(idUnit);
        return rolePreferenceRepository
                .findByGroupConfigurationIdConfigurationAndStudentNumber(configuration.getIdConfiguration(), studentNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "O aluno " + studentNumber + " ainda não submeteu preferências"));
    }

    /**
     * Saves the ranking of a student, creating it the first time and replacing it afterwards.
     * Every available role must appear exactly once, and only students of the unit can answer.
     */
    @Transactional
    public SubmitResult submitPreference(Long idUnit, String studentNumber, List<String> rankedRoles) {
        checkEnrolled(idUnit, studentNumber);
        GroupConfiguration configuration = groupConfigurationService.getConfiguration(idUnit);
        validateRanking(availableRoles(configuration), rankedRoles);

        return rolePreferenceRepository
                .findByGroupConfigurationIdConfigurationAndStudentNumber(configuration.getIdConfiguration(), studentNumber)
                .map(existing -> {
                    existing.updateRanking(rankedRoles);
                    return new SubmitResult(existing, false); // saved automatically when the transaction ends
                })
                .orElseGet(() -> new SubmitResult(
                        rolePreferenceRepository.save(new RolePreference(configuration, studentNumber, rankedRoles)),
                        true));
    }

    /**
     * The saved preference and whether it was created now (true) or updated (false).
     */
    public record SubmitResult(RolePreference preference, boolean created) {}

    private static List<String> availableRoles(GroupConfiguration configuration) {
        return List.copyOf(new LinkedHashSet<>(configuration.getRoles()));
    }

    /**
     * Refuses an invalid student number (400) or a student that is not associated with the unit (403).
     */
    private void checkEnrolled(Long idUnit, String studentNumber) {
        StudentNumbers.validate(studentNumber);
        if (!enrollmentService.isEnrolled(idUnit, studentNumber)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O aluno " + studentNumber + " não está associado a esta UC");
        }
    }

    /**
     * Refuses repeated roles, roles that do not belong to the assignment and missing roles.
     */
    private static void validateRanking(List<String> availableRoles, List<String> rankedRoles) {
        Set<String> seen = new HashSet<>();
        for (String role : rankedRoles) {
            if (!availableRoles.contains(role)) {
                throw badRequest("A role \"" + role + "\" não existe neste trabalho");
            }
            if (!seen.add(role)) {
                throw badRequest("A role \"" + role + "\" está repetida");
            }
        }
        List<String> missing = availableRoles.stream().filter(role -> !seen.contains(role)).toList();
        if (!missing.isEmpty()) {
            throw badRequest("Faltam ordenar as roles: " + String.join(", ", missing));
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
