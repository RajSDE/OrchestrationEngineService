package com.orchestrationengine.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component("SEND_NOTIFICATION")
public class SendNotificationService implements WorkflowStep {
    @Override
    public void execute(Map<String, Object> context) {
        // Example: send notification logic
        System.out.println("Sending notification...");
        context.put("notificationSent", true);
    }
}
