package com.orchestrationengine.ums.steps;

import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.service.WorkflowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("validate.user")
public class ValidateUserStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) {
        log.info("Validating user...");
    }

    @Override
    public void rollback(Map<String, Object> context) {
        log.info("Rollback for ValidateUserStep called");
        // No rollback needed for validation
    }
}
