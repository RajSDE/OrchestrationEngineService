package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component("VALIDATE_USER")
public class ValidateUserStep implements WorkflowStep {
    @Override
    public void execute(Map<String, Object> context) {
        // Simulate a validation failure for demonstration using error code
        context.put("validateUserStep", "User validation failed at " + System.currentTimeMillis());
        throw new WorkflowStepException("error.user.validation");
    }

    @Override
    public void rollback(Map<String, Object> context) {
        // No rollback needed for validation
    }
}
