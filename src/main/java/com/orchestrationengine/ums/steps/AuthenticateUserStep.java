package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.repository.UserCredentialsRepository;
import com.orchestrationengine.ums.entity.UserCredentials;
import com.orchestrationengine.ums.service.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates the username and password against database records.
 */
@Slf4j
@Component("authenticate.user")
@RequiredArgsConstructor
public class AuthenticateUserStep implements WorkflowStep {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordHashingService passwordHashingService;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Authenticating user credentials...");

        String username = (String) context.get("username");
        String password = (String) context.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "Username and password are required");
        }

        UserCredentials credentials = userCredentialsRepository.findByUsername(username)
                .orElseThrow(() -> new WorkflowStepException("INVALID_CREDENTIALS", "Invalid username or password"));

        if (credentials.getIsActive() != null && !credentials.getIsActive()) {
            throw new WorkflowStepException("ACCOUNT_DEACTIVATED", "This account has been deactivated");
        }

        if (credentials.getIsLocked() != null && credentials.getIsLocked()) {
            throw new WorkflowStepException("ACCOUNT_LOCKED", "This account is locked");
        }

        if (!passwordHashingService.verifyPassword(password, credentials.getPasswordHash())) {
            throw new WorkflowStepException("INVALID_CREDENTIALS", "Invalid username or password");
        }

        context.put("userProfileId", credentials.getUserProfileId());
        log.info("User {} successfully authenticated", username);
    }
}
