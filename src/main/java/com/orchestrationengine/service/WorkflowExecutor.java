package com.orchestrationengine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.orchestrationengine.config.WorkflowStepLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class WorkflowExecutor {
    private final ApplicationContext context;
    private final WorkflowStepLoader stepLoader;
    private final int maxRetries = 3; // Default retry count
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void executeWorkflowByServiceCode(String serviceCode, Map<String, Object> requestContext) {
        List<String> stepIds = stepLoader.loadStepsByServiceCode(serviceCode);
        ArrayList<WorkflowStep> executedSteps = new ArrayList<>();
        try {
            for (String stepId : stepIds) {
                WorkflowStep stepBean = (WorkflowStep) context.getBean(stepId);
                boolean success = false;
                int attempt = 0;
                Exception lastException = null;
                while (!success && attempt < maxRetries) {
                    Future<?> future = executorService.submit(() -> {
                        try {
                            stepBean.execute(requestContext);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    try {
                        future.get(); // Wait for async execution
                        success = true;
                    } catch (Exception e) {
                        attempt++;
                        lastException = e instanceof ExecutionException ? (Exception) e.getCause() : new Exception(e);
                        if (attempt >= maxRetries) {
                            throw lastException;
                        }
                    }
                }
                executedSteps.add(stepBean);
            }
        } catch (Exception ex) {
            // Rollback in reverse order
            for (int i = executedSteps.size() - 1; i >= 0; i--) {
                try {
                    executedSteps.get(i).rollback(requestContext);
                } catch (Exception rollbackEx) {
                    // Log rollback failure (could add logger)
                }
            }
            throw new RuntimeException("Workflow failed and rolled back", ex);
        }
    }
}
