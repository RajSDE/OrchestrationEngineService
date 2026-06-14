package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.entity.UserCredentials;
import com.orchestrationengine.ums.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Deactivates user credentials by setting isActive to false.
 */
@Slf4j
@Component("deactivate.user.status")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DeactivateUserStatusStep implements WorkflowStep {

    private final UserCredentialsRepository userCredentialsRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Deactivating user credentials status...");

        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String userIdStr = (String) request.get("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "User ID is required");
        }

        UUID profileId;
        try {
            profileId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new WorkflowStepException("INVALID_USER_ID", "Invalid user ID format");
        }

        UserCredentials credentials = userCredentialsRepository.findByUserProfileId(profileId)
                .orElseThrow(() -> new WorkflowStepException("USER_NOT_FOUND", "Credentials not found for user ID: " + profileId));

        credentials.setIsActive(false);
        userCredentialsRepository.save(credentials);

        log.info("User ID {} deactivated successfully", profileId);
    }
}
