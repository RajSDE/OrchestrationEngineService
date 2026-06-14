package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.entity.PasswordResetToken;
import com.orchestrationengine.ums.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validates recovery token status, usage, and expiry.
 */
@Slf4j
@Component("verify.reset.token")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class VerifyResetTokenStep implements WorkflowStep {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Verifying password reset token...");

        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String token = (String) request.get("token");
        if (token == null || token.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "Token is required");
        }

        PasswordResetToken resetTokenEntity = passwordResetTokenRepository.findByResetToken(token)
                .orElseThrow(() -> new WorkflowStepException("INVALID_TOKEN", "Reset token is invalid or does not exist"));

        if (resetTokenEntity.getIsUsed() != null && resetTokenEntity.getIsUsed()) {
            throw new WorkflowStepException("TOKEN_ALREADY_USED", "This reset token has already been used");
        }

        if (resetTokenEntity.getExpiryTime() != null && resetTokenEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new WorkflowStepException("TOKEN_EXPIRED", "This reset token has expired");
        }

        context.put("userProfileId", resetTokenEntity.getUserProfileId());
        context.put("resetTokenEntityId", resetTokenEntity.getId());
        log.info("Password reset token verified for userProfileId: {}", resetTokenEntity.getUserProfileId());
    }
}
