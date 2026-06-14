package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.entity.PasswordResetToken;
import com.orchestrationengine.ums.entity.UserProfile;
import com.orchestrationengine.ums.repository.PasswordResetTokenRepository;
import com.orchestrationengine.ums.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Validates recovery email, generates reset token, and stores it in the database.
 */
@Slf4j
@Component("generate.reset.token")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateResetTokenStep implements WorkflowStep {

    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Generating password reset token...");

        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String email = (String) request.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "Email is required");
        }

        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new WorkflowStepException("EMAIL_NOT_FOUND", "No user found with email: " + email));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetTokenEntity = new PasswordResetToken();
        resetTokenEntity.setUserProfileId(profile.getUserProfileId());
        resetTokenEntity.setResetToken(token);
        // Reset token expires in 1 hour
        resetTokenEntity.setExpiryTime(LocalDateTime.now().plusHours(1));
        resetTokenEntity.setIsUsed(false);

        passwordResetTokenRepository.save(resetTokenEntity);

        context.put("userProfileId", profile.getUserProfileId());
        context.put("userProfile", profile);
        context.put("resetToken", token);

        log.info("Password reset token generated and saved for email: {}", email);
    }
}
