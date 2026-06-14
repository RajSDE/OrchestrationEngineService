package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simulates sending password reset instructions via email asynchronously.
 */
@Slf4j
@Component("send.reset.notification")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class SendResetNotificationStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Preparing to send password reset notification email...");

        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String email = (String) request.get("email");
        String resetToken = (String) context.get("resetToken");

        if (email == null || resetToken == null) {
            log.warn("Cannot send reset email: missing email or token in context.");
            return;
        }

        // Simulate network/SMTP latency
        Thread.sleep(1000);

        log.info("SUCCESS: Password reset email containing link http://localhost:8080/v1/user/reset-password?token={} successfully sent to {}", resetToken, email);
        context.put("resetNotificationSent", true);
    }
}
