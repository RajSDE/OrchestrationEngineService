package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import com.orchestrationengine.repository.UserAuthRepository;
import com.orchestrationengine.ums.entity.UserAuth;
import com.orchestrationengine.ums.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Generates JWT Access and Refresh tokens, and persists the refresh token in the database.
 */
@Slf4j
@Component("generate.auth.tokens")
@RequiredArgsConstructor
public class GenerateAuthTokensStep implements WorkflowStep {

    private final JwtTokenService jwtTokenService;
    private final UserAuthRepository userAuthRepository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Generating authentication tokens...");

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

        String accessToken = jwtTokenService.generateAccessToken(profileId, username);
        String refreshToken = jwtTokenService.generateRefreshToken(profileId);

        // Store refresh token in user_auth table
        UserAuth userAuth = new UserAuth();
        userAuth.setUserProfileId(profileId);
        userAuth.setRefreshToken(refreshToken);
        // Refresh token expires in 7 days
        userAuth.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userAuth.setIsRevoked(false);

        userAuthRepository.save(userAuth);

        context.put("accessToken", accessToken);
        context.put("refreshToken", refreshToken);

        log.info("Access and Refresh tokens generated successfully");
    }
}
