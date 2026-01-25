package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.orchestrationengine.ums.service.UserService;

import java.util.Map;

@Slf4j
@Component("register.user")
@RequiredArgsConstructor
public class RegisterUserStep implements WorkflowStep {

    private UserService userService;

    @Override
    public void execute(Map<String, Object> context) {
        log.info("Registering user...");
    }

    @Override
    public void rollback(Map<String, Object> context) {
        // Implement rollback logic if needed (e.g., delete user if created)
    }
}
