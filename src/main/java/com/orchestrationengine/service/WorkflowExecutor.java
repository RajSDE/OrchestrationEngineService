package com.orchestrationengine.service;

import com.orchestrationengine.exception.WorkflowStepException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.orchestrationengine.config.WorkflowStepLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WorkflowExecutor {
    private final ApplicationContext context;
    private final WorkflowStepLoader stepLoader;
    private final int maxRetries = 3; // Default retry count
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Autowired
    private MessageSource messageSource;

    public void executeWorkflowByServiceCode(String serviceCode, Map<String, Object> requestContext, String language) {
        List<String> stepIds = stepLoader.loadStepsByServiceCode(serviceCode);
        ArrayList<WorkflowStep> executedSteps = new ArrayList<>();
        Locale locale = (language != null && !language.isEmpty()) ? Locale.forLanguageTag(language) : Locale.getDefault();
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
                            Map<String, Object> error = new HashMap<>();
                            String errorCode = "WF_STEP_FAIL";
                            String errorMessage = lastException.getMessage();
                            Throwable cause = lastException;
                            if (lastException instanceof RuntimeException && lastException.getCause() instanceof WorkflowStepException) {
                                cause = lastException.getCause();
                            }
                            if (cause instanceof WorkflowStepException) {
                                errorCode = ((WorkflowStepException) cause).getErrorCode();
                                errorMessage = messageSource.getMessage(errorCode, null, errorCode, locale);
                            }
                            error.put("code", errorCode);
                            error.put("message", errorMessage);
                            error.put("component", stepId);
                            requestContext.put("error", error);
                            throw lastException;
                        }
                    }
                }
                executedSteps.add(stepBean);
            }
            requestContext.put("workflowStatus", "SUCCESS");
        } catch (Exception ex) {
            // Rollback previously executed steps in reverse order
            for (int i = executedSteps.size() - 1; i >= 0; i--) {
                try {
                    executedSteps.get(i).rollback(requestContext);
                } catch (Exception rollbackEx) {
                    // Optionally log rollback failure
                }
            }
            requestContext.put("status", "FAILED");
            // Optionally log or rethrow the workflow failure
        }
    }
}
