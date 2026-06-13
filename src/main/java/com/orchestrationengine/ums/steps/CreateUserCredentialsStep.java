package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.repository.UserCredentialsRepository;
import com.orchestrationengine.ums.entity.UserCredentials;
import com.orchestrationengine.ums.service.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Creates and stores credentials for the newly registered user profile.
 */
@Slf4j
@Component("create.user.credentials")
@RequiredArgsConstructor
public class CreateUserCredentialsStep implements WorkflowStep {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordHashingService passwordHashingService;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Creating user credentials in database...");

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

        String username = (String) context.get("username");
        String password = (String) context.get("password");

        String passwordHash = passwordHashingService.hashPassword(password);

        UserCredentials credentials = new UserCredentials();
        credentials.setUserProfileId(profileId);
        credentials.setUsername(username);
        credentials.setPasswordHash(passwordHash);
        credentials.setIsActive(true);
        credentials.setIsLocked(false);

        userCredentialsRepository.save(credentials);
        log.info("User credentials successfully created for username: {}", username);
    }

    @Override
    @Transactional
    public void rollback(Map<String, Object> context) {
        Object profileIdObj = context.get("userProfileId");
        if (profileIdObj != null) {
            UUID profileId = null;
            if (profileIdObj instanceof UUID) {
                profileId = (UUID) profileIdObj;
            } else if (profileIdObj instanceof String) {
                try {
                    profileId = UUID.fromString((String) profileIdObj);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to parse UUID for credentials rollback: {}", profileIdObj);
                }
            }
            if (profileId != null) {
                log.info("Compensating Transaction: Deleting credentials for user profile ID: {}", profileId);
                userCredentialsRepository.deleteByUserProfileId(profileId);
            }
        }
    }
}
