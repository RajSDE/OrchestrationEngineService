package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.repository.UserCredentialsRepository;
import com.orchestrationengine.repository.PasswordResetTokenRepository;
import com.orchestrationengine.ums.entity.UserCredentials;
import com.orchestrationengine.ums.entity.PasswordResetToken;
import com.orchestrationengine.ums.service.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Hashes and updates user password, and marks the reset token as used.
 */
@Slf4j
@Component("update.password")
@RequiredArgsConstructor
public class UpdatePasswordStep implements WorkflowStep {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHashingService passwordHashingService;

    @Override
    @Transactional
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Updating user password...");

        Object profileIdObj = context.get("userProfileId");
        UUID profileId = null;
        if (profileIdObj instanceof UUID) {
            profileId = (UUID) profileIdObj;
        } else if (profileIdObj instanceof String) {
            profileId = UUID.fromString((String) profileIdObj);
        }

        if (profileId == null) {
            throw new IllegalStateException("UserProfileId not found in workflow context");
        }

        String newPassword = (String) context.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "New password is required");
        }

        UserCredentials credentials = userCredentialsRepository.findByUserProfileId(profileId)
                .orElseThrow(() -> new WorkflowStepException("CREDENTIALS_NOT_FOUND", "Credentials not found for profile"));

        credentials.setPasswordHash(passwordHashingService.hashPassword(newPassword));
        credentials.setPasswordChangedAt(LocalDateTime.now());
        userCredentialsRepository.save(credentials);

        // Mark reset token as used
        Long tokenId = (Long) context.get("resetTokenEntityId");
        if (tokenId != null) {
            PasswordResetToken tokenEntity = passwordResetTokenRepository.findById(tokenId).orElse(null);
            if (tokenEntity != null) {
                tokenEntity.setIsUsed(true);
                passwordResetTokenRepository.save(tokenEntity);
            }
        }

        log.info("User password updated successfully and reset token marked as used");
    }
}
