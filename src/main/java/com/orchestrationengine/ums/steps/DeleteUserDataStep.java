package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.repository.PasswordResetTokenRepository;
import com.orchestrationengine.ums.repository.UserAuthRepository;
import com.orchestrationengine.ums.repository.UserCredentialsRepository;
import com.orchestrationengine.ums.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Clean up user data cascading across all database tables.
 */
@Slf4j
@Component("delete.user.data")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DeleteUserDataStep implements WorkflowStep {

    private final UserProfileRepository userProfileRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Deleting user data...");

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

        if (!userProfileRepository.existsById(profileId)) {
            throw new WorkflowStepException("USER_NOT_FOUND", "No user found with ID: " + profileId);
        }

        // Clean up child rows manually to ensure clean ORM state
        userAuthRepository.deleteByUserProfileId(profileId);
        passwordResetTokenRepository.deleteByUserProfileId(profileId);
        userCredentialsRepository.deleteByUserProfileId(profileId);
        userProfileRepository.deleteById(profileId);

        log.info("User data for ID {} deleted successfully", profileId);
    }
}
