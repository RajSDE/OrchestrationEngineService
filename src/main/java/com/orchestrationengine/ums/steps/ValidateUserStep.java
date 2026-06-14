package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.repository.UserCredentialsRepository;
import com.orchestrationengine.ums.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates registration input fields and checks database constraints.
 */
@Slf4j
@Component("validate.user")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class ValidateUserStep implements WorkflowStep {

    private final UserProfileRepository userProfileRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Validating user registration payload...");

        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String username = (String) request.get("username");
        if (username != null && username.trim().isEmpty()) {
            context.remove("username");
            username = null;
        }
        String email = (String) request.get("email");
        String password = (String) request.get("password");

        if (email == null || email.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new WorkflowStepException("INVALID_INPUT", "Password cannot be empty");
        }

        if (username != null && !username.trim().isEmpty()) {
            if (userCredentialsRepository.existsByUsername(username)) {
                throw new WorkflowStepException("USERNAME_ALREADY_EXISTS", "Username already exists: " + username);
            }
        }

        if (userProfileRepository.existsByEmail(email)) {
            throw new WorkflowStepException("EMAIL_ALREADY_EXISTS", "Email already exists: " + email);
        }

        log.info("Validation successful for username: {} and email: {}", username, email);
    }
}
