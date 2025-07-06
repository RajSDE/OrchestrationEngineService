package com.orchestrationengine.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component("VALIDATE_USER")
public class ValidateUserService implements WorkflowStep {
    @Override
    public void execute(Map<String, Object> context) {
        // Example: validate user logic
        System.out.println("Validating user...");
        context.put("userValidated", true);
    }
}
