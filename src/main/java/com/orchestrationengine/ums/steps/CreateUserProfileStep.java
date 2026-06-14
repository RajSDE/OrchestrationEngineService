package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.ums.repository.UserProfileRepository;
import com.orchestrationengine.ums.entity.UserProfile;
import com.orchestrationengine.util.SequencedUuidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Persists the basic user profile info in the user_profile table.
 * Generates an optimized, time-ordered UUIDv7 as the primary key.
 */
@Slf4j
@Component("create.user.profile")
@RequiredArgsConstructor
public class CreateUserProfileStep implements WorkflowStep {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Creating user profile in database...");

        UUID profileId = SequencedUuidGenerator.generateV7();
        String email = (String) context.get("email");
        String firstName = (String) context.get("firstName");
        String middleName = (String) context.get("middleName");
        String lastName = (String) context.get("lastName");
        String mobileNumber = (String) context.get("mobileNumber");
        String preferredLanguage = (String) context.get("preferredLanguage");
        String gender = (String) context.get("gender");

        String fullName = (firstName != null ? firstName : "") + 
                           (middleName != null && !middleName.trim().isEmpty() ? " " + middleName : "") + 
                           (lastName != null && !lastName.trim().isEmpty() ? " " + lastName : "");
        fullName = fullName.trim();

        UserProfile profile = UserProfile.builder()
                .userProfileId(profileId)
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .fullName(fullName.isEmpty() ? null : fullName)
                .email(email)
                .mobileNumber(mobileNumber)
                .preferredLanguage(preferredLanguage)
                .gender(gender)
                .build();

        userProfileRepository.save(profile);
        context.put("userProfileId", profileId);
        log.info("User profile successfully created with ID: {}", profileId);
    }

    @Override
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
                    log.error("Failed to parse UUID for profile rollback: {}", profileIdObj);
                }
            }
            if (profileId != null) {
                log.info("Compensating Transaction: Deleting user profile ID: {}", profileId);
                userProfileRepository.deleteById(profileId);
                context.remove("userProfileId");
            }
        }
    }
}
