package com.orchestrationengine.service;

import com.orchestrationengine.config.MdcFilter;
import com.orchestrationengine.config.WorkflowStepLoader;
import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.model.WorkflowStepDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutor {

    private final ApplicationContext context;
    private final WorkflowStepLoader stepLoader;
    private final ThreadPoolTaskExecutor workflowAsyncPool;
    private final ThreadPoolTaskExecutor stepExecutionPool;

    private record ExecutedStep(WorkflowStep stepBean, WorkflowStepDefinition def) {
    }

    public void executeWorkflowByServiceCode(String serviceCode, Map<String, Object> requestContext, String language) {
        List<WorkflowStepDefinition> stepDefs = stepLoader.getSteps(serviceCode);
        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        requestContext.put("traceId", traceId);
        if (stepDefs == null || stepDefs.isEmpty()) {
            log.error("No workflow steps found for serviceCode: {}", serviceCode);
            failWorkflow(requestContext, "WORKFLOW_NOT_FOUND", "No workflow definition found for service: " + serviceCode);
            return;
        }

        List<ExecutedStep> executedSteps = new ArrayList<>();
        String currentStepId = null;

        try {
            for (WorkflowStepDefinition stepDef : stepDefs) {
                currentStepId = stepDef.getId();

                if (!stepDef.isEnabled()) continue;

                WorkflowStep stepBean;
                try {
                    stepBean = context.getBean(stepDef.getId(), WorkflowStep.class);
                } catch (NoSuchBeanDefinitionException e) {
                    throw new RuntimeException("Bean not found for step: " + stepDef.getId(), e);
                }

                log.info("Executing step: {}, for: {}", stepDef.getId(), stepDef.getName());
                if (stepDef.isAsync()) {
                    workflowAsyncPool.submit(() -> {
                        try {
                            log.info("Executing async step: {}", stepDef.getId());
                            executeStep(stepBean, requestContext, stepDef);
                        } catch (Exception e) {
                            log.error("Async step [{}] failed", stepDef.getId(), e);
                        }
                    });
                } else {
                    executedSteps.add(new ExecutedStep(stepBean, stepDef));
                    executeStep(stepBean, requestContext, stepDef);
                }
            }
            requestContext.put("status", "SUCCESS");

        } catch (Exception e) {
            handleException(e, requestContext, executedSteps, currentStepId);
        }
    }

    private void executeStep(WorkflowStep step, Map<String, Object> ctx, WorkflowStepDefinition def) throws Exception {
        int maxRetries = def.isRetry() ? 3 : 1;
        int attempts = 0;
        // Default timeout 10 seconds if not defined in XML
        long timeout = def.getTimeout() > 0 ? def.getTimeout() : 10000;
        Exception lastException = null;
        Future<?> future = null;
        while (attempts < maxRetries) {
            try {
                future = stepExecutionPool.submit(() -> {
                    try {
                        step.execute(ctx);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                future.get(timeout, TimeUnit.MILLISECONDS);

                return;

            } catch (TimeoutException e) {
                future.cancel(true);
                attempts++;
                lastException = new RuntimeException("Step timed out after " + timeout + "ms");

            } catch (ExecutionException e) {
                attempts++;
                Throwable cause = e.getCause();
                lastException = (cause instanceof Exception) ? (Exception) cause : new Exception(cause);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
            if (attempts < maxRetries) {
                log.warn("Retrying step {} due to error: {}", def.getId(), lastException.getMessage());
                Thread.sleep(100);
            }
        }

        throw lastException;
    }

    private void handleException(Exception e, Map<String, Object> requestContext, List<ExecutedStep> executedSteps, String failedComponentId) {
        log.error("Workflow failed at step: {}", failedComponentId, e);
        Map<String, Object> errorDetails = getErrorInformation(e, failedComponentId);
        requestContext.put("error", errorDetails);
        requestContext.put("status", "FAILED");

        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            ExecutedStep item = executedSteps.get(i);

            if (item.def().isRollback()) {
                try {
                    log.info("Rolling back step: {}", item.def().getId());
                    item.stepBean().rollback(requestContext);
                } catch (Exception ignored) {
                    log.warn("Rollback failed for step: {}", item.def().getId());
                }
            } else {
                log.info("Skipping rollback for step: {} (rollback=false)", item.def().getId());
            }
        }
    }

    private void failWorkflow(Map<String, Object> ctx, String code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("component", "ORCHESTRATOR");
        error.put("code", code);
        error.put("message", message);
        ctx.put("error", error);
        ctx.put("status", "FAILED");
    }

    private static Map<String, Object> getErrorInformation(Exception e, String failedComponentId) {
        Map<String, Object> errorDetails = new HashMap<>();
        Throwable cause = e instanceof RuntimeException && e.getCause() != null ? e.getCause() : e;

        errorDetails.put("component", failedComponentId != null ? failedComponentId : "UNKNOWN");

        if (cause instanceof WorkflowStepException wse) {
            errorDetails.put("code", wse.getMessage());
            errorDetails.put("message", "Validation failed");
        } else {
            errorDetails.put("code", "INTERNAL_SYSTEM_ERROR");
            errorDetails.put("message", cause.getMessage());
        }
        return errorDetails;
    }
}