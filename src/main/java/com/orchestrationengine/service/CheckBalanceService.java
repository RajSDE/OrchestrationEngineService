package com.orchestrationengine.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component("CHECK_BALANCE")
public class CheckBalanceService implements WorkflowStep {
    @Override
    public void execute(Map<String, Object> context) {
        // Example: check balance logic
        System.out.println("Checking balance...");
        context.put("balanceChecked", true);
    }
}
