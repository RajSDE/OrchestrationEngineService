package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.orchestrationengine.ums.service.UserService;

import java.util.Map;

@Component("REGISTER_USER")
public class RegisterUserStep implements WorkflowStep {
    @Autowired
    private UserService userService;

    @Override
    public void execute(Map<String, Object> context) {
        // Call user registration logic
//        userService.registerUser(context);
        // For testing: append data to context
        context.put("registerUserStep", "User registered at " + System.currentTimeMillis());
    }

    @Override
    public void rollback(Map<String, Object> context) {
        // Implement rollback logic if needed (e.g., delete user if created)
    }
}
