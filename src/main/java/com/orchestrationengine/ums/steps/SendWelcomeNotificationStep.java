package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simulates sending a welcome email/notification to the newly registered user.
 * Runs asynchronously within the workflowAsyncPool.
 */
@Slf4j
@Component("send.welcome.notification")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class SendWelcomeNotificationStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        log.info("Preparing to send welcome notification...");
        
        Map<String, Object> request = (Map<String, Object>) context.get("request");
        if (request == null) {
            request = context;
        }

        String username = (String) request.get("username");
        String email = (String) request.get("email");

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
