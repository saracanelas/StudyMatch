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
import java.util.regex.Pattern;

@Service
public class RolePreferenceService {

    private static final Pattern STUDENT_NUMBER = Pattern.compile("[A-Za-z0-9]{1,20}");

    private final RolePreferenceRepository rolePreferenceRepository;
    private final GroupConfigurationService groupConfigurationService;

    /**
     * Receives the repository of preferences and the service used to find the group configuration.
     */
    public RolePreferenceService(RolePreferenceRepository rolePreferenceRepository,
                                 GroupConfigurationService groupConfigurationService) {
        this.rolePreferenceRepository = rolePreferenceRepository;
        this.groupConfigurationService = groupConfigurationService;
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
     * Returns the preference a student already submitted for the unit, or 404 if there is none.
     */
    @Transactional(readOnly = true)
    public RolePreference getPreference(Long idUnit, String studentNumber) {
        validateStudentNumber(studentNumber);
        GroupConfiguration configuration = groupConfigurationService.getConfiguration(idUnit);
        return rolePreferenceRepository
                .findByGroupConfigurationIdConfigurationAndStudentNumber(configuration.getIdConfiguration(), studentNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "O aluno " + studentNumber + " ainda não submeteu preferências"));
    }

    /**
     * Saves the ranking of a student, creating it the first time and replacing it afterwards.
     * Every available role must appear exactly once.
     */
    @Transactional
    public SubmitResult submitPreference(Long idUnit, String studentNumber, List<String> rankedRoles) {
        validateStudentNumber(studentNumber);
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

    private static void validateStudentNumber(String studentNumber) {
        if (studentNumber == null || !STUDENT_NUMBER.matcher(studentNumber).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O número de aluno só pode ter letras e números (máximo 20)");
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
