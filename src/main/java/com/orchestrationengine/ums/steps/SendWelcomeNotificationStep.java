package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simulates sending a welcome email/notification to the newly registered user.
 * Runs asynchronously within the workflowAsyncPool.
 */
@Slf4j
@Component("send.welcome.notification")
public class SendWelcomeNotificationStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Preparing to send welcome notification...");
        
        String username = (String) context.get("username");
        String email = (String) context.get("email");

        if (email == null) {
            log.warn("Cannot send notification: email is missing in context.");
            return;
        }

        // Simulate network/SMTP latency
        Thread.sleep(1000);

        log.info("SUCCESS: Welcome notification email successfully sent to {} ({})", username, email);
        context.put("welcomeNotificationSent", true);
    }
}
