package com.orchestrationengine.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component("DEBIT_ACCOUNT")
public class DebitAccountService implements WorkflowStep {
    @Override
    public void execute(Map<String, Object> context) {
        // Example: debit account logic
        System.out.println("Debiting account...");
        context.put("accountDebited", true);
    }
}
