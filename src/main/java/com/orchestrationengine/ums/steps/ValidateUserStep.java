package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("VALIDATE_USER")
public class ValidateUserStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) {
        log.info("Validating user...");
        // Simulate a validation failure for demonstration using error code
        context.put("validateUserStep", "User validation failed at " + System.currentTimeMillis());
//        throw new WorkflowStepException("error.user.validation");
    }

    @Override
    public void rollback(Map<String, Object> context) {
        log.info("Rollback for ValidateUserStep called");
        // No rollback needed for validation
    }
}
