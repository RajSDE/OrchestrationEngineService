package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simulates sending password reset instructions via email asynchronously.
 */
@Slf4j
@Component("send.reset.notification")
public class SendResetNotificationStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Preparing to send password reset notification email...");

        String email = (String) context.get("email");
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
