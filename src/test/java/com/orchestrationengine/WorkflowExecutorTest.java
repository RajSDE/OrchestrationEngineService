package com.orchestrationengine;

import com.orchestrationengine.repository.*;
import com.orchestrationengine.ums.repository.*;
import com.orchestrationengine.service.WorkflowExecutor;
import com.orchestrationengine.ums.entity.UserAuth;
import com.orchestrationengine.ums.entity.UserCredentials;
import com.orchestrationengine.ums.entity.UserProfile;
import com.orchestrationengine.ums.entity.PasswordResetToken;
import com.orchestrationengine.ums.service.JwtTokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WorkflowExecutorTest {

    @Autowired
    private WorkflowExecutor workflowExecutor;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    public void setup() {
        userAuthRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        userCredentialsRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @Test
    public void testWorkflowDatabaseMappingsAndRuntimeToggles() {
        // Assert that workflows are seeded in database workflows table
        assertTrue(workflowRepository.existsById("USER_REGISTRATION"));
        assertTrue(workflowRepository.existsById("USER_LOGIN"));
        assertTrue(workflowRepository.existsById("FORGOT_PASSWORD"));
        assertTrue(workflowRepository.existsById("RESET_PASSWORD"));
        assertTrue(workflowRepository.existsById("DELETE_USER"));
        assertTrue(workflowRepository.existsById("DEACTIVATE_USER"));

        // Register a user first to test login toggles
        Map<String, Object> registerCtx = new ConcurrentHashMap<>();
        registerCtx.put("username", "toggleuser");
        registerCtx.put("email", "toggle@example.com");
        registerCtx.put("password", "MyPassword123");
        workflowExecutor.executeWorkflowByServiceCode("USER_REGISTRATION", registerCtx, "en");
        assertEquals("SUCCESS", registerCtx.get("status"));

        // Disable USER_LOGIN in the workflows table
        com.orchestrationengine.model.Workflow loginWorkflow = workflowRepository.findById("USER_LOGIN").orElseThrow();
        loginWorkflow.setEnabled("N");
        workflowRepository.save(loginWorkflow);

        // Try to login - should fail because the workflow is disabled in the DB
        Map<String, Object> loginCtx = new ConcurrentHashMap<>();
        loginCtx.put("username", "toggleuser");
        loginCtx.put("password", "MyPassword123");
        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", loginCtx, "en");
        assertEquals("FAILED", loginCtx.get("status"));
        Map<?, ?> error = (Map<?, ?>) loginCtx.get("error");
        assertEquals("WORKFLOW_DISABLED", error.get("code"));

        // Re-enable USER_LOGIN
        loginWorkflow.setEnabled("Y");
        workflowRepository.save(loginWorkflow);

        // Try to login again - should succeed now
        Map<String, Object> loginCtx2 = new ConcurrentHashMap<>();
        loginCtx2.put("username", "toggleuser");
        loginCtx2.put("password", "MyPassword123");
        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", loginCtx2, "en");
        assertEquals("SUCCESS", loginCtx2.get("status"));
    }

    @Test
    public void testCompleteUserLifecycle() {
        // 1. REGISTER
        Map<String, Object> registerCtx = new ConcurrentHashMap<>();
        registerCtx.put("username", "lifecycleuser");
        registerCtx.put("email", "lifecycle@example.com");
        registerCtx.put("password", "MyPassword123");
        registerCtx.put("firstName", "Lifecycle");
        registerCtx.put("lastName", "Tester");

        workflowExecutor.executeWorkflowByServiceCode("USER_REGISTRATION", registerCtx, "en");
        assertEquals("SUCCESS", registerCtx.get("status"));
        UUID profileId = (UUID) registerCtx.get("userProfileId");
        assertNotNull(profileId);

        // 2. LOGIN
        Map<String, Object> loginCtx = new ConcurrentHashMap<>();
        loginCtx.put("username", "lifecycleuser");
        loginCtx.put("password", "MyPassword123");

        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", loginCtx, "en");
        assertEquals("SUCCESS", loginCtx.get("status"));
        
        String accessToken = (String) loginCtx.get("accessToken");
        String refreshToken = (String) loginCtx.get("refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Verify tokens
        DecodedJWT decodedJWT = jwtTokenService.verifyToken(accessToken);
        assertEquals(profileId.toString(), decodedJWT.getSubject());
        assertEquals("lifecycleuser", decodedJWT.getClaim("username").asString());

        // Verify user auth table insertion
        List<UserAuth> activeSessions = userAuthRepository.findAll();
        assertEquals(1, activeSessions.size());
        assertEquals(profileId, activeSessions.get(0).getUserProfileId());
        assertEquals(refreshToken, activeSessions.get(0).getRefreshToken());

        // 3. FORGOT PASSWORD
        Map<String, Object> forgotCtx = new ConcurrentHashMap<>();
        forgotCtx.put("email", "lifecycle@example.com");

        workflowExecutor.executeWorkflowByServiceCode("FORGOT_PASSWORD", forgotCtx, "en");
        assertEquals("SUCCESS", forgotCtx.get("status"));
        String resetToken = (String) forgotCtx.get("resetToken");
        assertNotNull(resetToken);

        // Verify reset token table insertion
        List<PasswordResetToken> resetTokens = passwordResetTokenRepository.findAll();
        assertEquals(1, resetTokens.size());
        assertEquals(resetToken, resetTokens.get(0).getResetToken());
        assertFalse(resetTokens.get(0).getIsUsed());

        // 4. RESET PASSWORD
        Map<String, Object> resetCtx = new ConcurrentHashMap<>();
        resetCtx.put("token", resetToken);
        resetCtx.put("newPassword", "NewPassword999");

        workflowExecutor.executeWorkflowByServiceCode("RESET_PASSWORD", resetCtx, "en");
        assertEquals("SUCCESS", resetCtx.get("status"));

        // Verify token marked used
        PasswordResetToken updatedToken = passwordResetTokenRepository.findById(resetTokens.get(0).getId()).orElseThrow();
        assertTrue(updatedToken.getIsUsed());

        // 5. LOGIN WITH NEW PASSWORD
        Map<String, Object> newLoginCtx = new ConcurrentHashMap<>();
        newLoginCtx.put("username", "lifecycleuser");
        newLoginCtx.put("password", "NewPassword999");

        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", newLoginCtx, "en");
        assertEquals("SUCCESS", newLoginCtx.get("status"));

        // 6. DEACTIVATE USER
        Map<String, Object> deactivateCtx = new ConcurrentHashMap<>();
        deactivateCtx.put("userId", profileId.toString());

        workflowExecutor.executeWorkflowByServiceCode("DEACTIVATE_USER", deactivateCtx, "en");
        assertEquals("SUCCESS", deactivateCtx.get("status"));

        // Verify login fails now
        Map<String, Object> failedLoginCtx = new ConcurrentHashMap<>();
        failedLoginCtx.put("username", "lifecycleuser");
        failedLoginCtx.put("password", "NewPassword999");

        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", failedLoginCtx, "en");
        assertEquals("FAILED", failedLoginCtx.get("status"));
        Map<?, ?> error = (Map<?, ?>) failedLoginCtx.get("error");
        assertEquals("ACCOUNT_DEACTIVATED", error.get("code"));

        // 7. DELETE USER
        Map<String, Object> deleteCtx = new ConcurrentHashMap<>();
        deleteCtx.put("userId", profileId.toString());

        workflowExecutor.executeWorkflowByServiceCode("DELETE_USER", deleteCtx, "en");
        assertEquals("SUCCESS", deleteCtx.get("status"));

        // Verify clean database state (cascading deletes)
        assertEquals(0, userProfileRepository.count());
        assertEquals(0, userCredentialsRepository.count());
        assertEquals(0, userAuthRepository.count());
        assertEquals(0, passwordResetTokenRepository.count());
    }
}
