package com.orchestrationengine.service;

import com.orchestrationengine.config.MdcFilter;
import com.orchestrationengine.config.WorkflowStepLoader;
import com.orchestrationengine.exception.WorkflowStepException;
import com.orchestrationengine.model.WorkflowStepDefinition;
import com.orchestrationengine.model.ServiceRequest;
import com.orchestrationengine.repository.WorkflowRepository;
import com.orchestrationengine.repository.ServiceRequestRepository;
import com.orchestrationengine.dto.ErrorResponseDto;
import com.orchestrationengine.dto.GenericActionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.*;
import java.util.concurrent.*;

/**
 * WorkflowExecutor is a orchestration component that executes workflow step definitions as commands.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutor {

    public static final String WORKFLOW_NOT_FOUND = "WORKFLOW_NOT_FOUND";
    private final ApplicationContext context;
    private final WorkflowStepLoader stepLoader;
    private final ThreadPoolTaskExecutor workflowAsyncPool;
    private final ThreadPoolTaskExecutor stepExecutionPool;
    private final WorkflowRepository workflowRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${service.request.record:false}")
    private boolean recordRequests;

    @org.springframework.beans.factory.annotation.Value("${service.request.workflows:}")
    private String recordWorkflowsConfig;

    private static class ExecutedStep {
        private final WorkflowStep stepBean;
        private final WorkflowStepDefinition def;

        public ExecutedStep(WorkflowStep stepBean, WorkflowStepDefinition def) {
            this.stepBean = stepBean;
            this.def = def;
        }

        public WorkflowStep stepBean() {
            return stepBean;
        }

        public WorkflowStepDefinition def() {
            return def;
        }
    }

    public void executeWorkflowByServiceCode(String serviceCode, Map<String, Object> requestContext, String language) {
        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        requestContext.put("traceId", traceId);
        requestContext.put("serviceCode", serviceCode);

        boolean isRecordable = shouldRecord(serviceCode);
        java.time.LocalDateTime requestTime = java.time.LocalDateTime.now();
        String initialRequestPayload = isRecordable ? getCleanRequestPayload(requestContext) : null;

        try {
            // Database runtime toggle check
            var dbWorkflow = workflowRepository.findById(serviceCode);
            if (dbWorkflow.isEmpty() || !"Y".equalsIgnoreCase(dbWorkflow.get().getEnabled())) {
                log.error("Workflow serviceCode: {} is disabled or not registered in database", serviceCode);
                failWorkflow(requestContext, "WORKFLOW_DISABLED", "Workflow is disabled or not registered in database: " + serviceCode);
                return;
            }

            List<WorkflowStepDefinition> stepDefs = stepLoader.getSteps(serviceCode);
            if (stepDefs == null || stepDefs.isEmpty()) {
                log.error("No workflow steps found for serviceCode: {}", serviceCode);
                failWorkflow(requestContext, WORKFLOW_NOT_FOUND, "No workflow definition found for service: " + serviceCode);
                return;
            }

            List<ExecutedStep> executedSteps = new ArrayList<>();
            String currentStepId = null;

            try {
                for (WorkflowStepDefinition stepDef : stepDefs) {
                    currentStepId = stepDef.getId();

                    if (!stepDef.isEnabled()) {
                        requestContext.put(stepDef.getId(), "DISABLED");
                        continue;
                    }
                    requestContext.put(stepDef.getId(), "IN_PROGRESS");
                    WorkflowStep stepBean;
                    try {
                        stepBean = context.getBean(stepDef.getId(), WorkflowStep.class);
                    } catch (NoSuchBeanDefinitionException e) {
                        throw new RuntimeException("Bean not found for step: " + stepDef.getId(), e);
                    }

                    log.info("Executing step: {}, for: {}", stepDef.getId(), stepDef.getName());
                    if (stepDef.isAsync()) {
                        requestContext.put(stepDef.getId(), "SUBMITTED");
                        workflowAsyncPool.submit(() -> {
                            try {
                                log.info("Executing async step: {}", stepDef.getId());
                                executeStep(stepBean, requestContext, stepDef);
                                requestContext.put(stepDef.getId(), "SUCCESS");
                            } catch (Exception e) {
                                requestContext.put(stepDef.getId(), "FAILED");
                                log.error("Async step [{}] failed", stepDef.getId(), e);
                            }
                        });
                    } else {
                        executedSteps.add(new ExecutedStep(stepBean, stepDef));
                        executeStep(stepBean, requestContext, stepDef);
                        requestContext.put(stepDef.getId(), "SUCCESS");
                    }
                }
                requestContext.put("status", "SUCCESS");

            } catch (Exception e) {
                handleException(e, requestContext, executedSteps, currentStepId);
            }
        } finally {
            if (isRecordable) {
                try {
                    java.time.LocalDateTime responseTime = java.time.LocalDateTime.now();
                    long durationMs = java.time.Duration.between(requestTime, responseTime).toMillis();
                    String status = (String) requestContext.get("status");
                    if (status == null) status = "FAILED";

                    String responsePayloadJson;
                    if ("SUCCESS".equals(status)) {
                        Object responseBody = requestContext.get("responseBody");
                        responsePayloadJson = responseBody != null ? objectMapper.writeValueAsString(responseBody) : "{}";
                    } else {
                        Object error = requestContext.get("error");
                        responsePayloadJson = error != null ? objectMapper.writeValueAsString(error) : "{}";
                    }

                    ServiceRequest serviceRequest = ServiceRequest.builder()
                            .traceId(traceId)
                            .serviceCode(serviceCode)
                            .requestPayload(initialRequestPayload)
                            .responsePayload(responsePayloadJson)
                            .status(status)
                            .requestTime(requestTime)
                            .responseTime(responseTime)
                            .durationMs(durationMs)
                            .build();

                    serviceRequestRepository.save(serviceRequest);
                    log.info("Saved service request log to database for serviceCode: {}, traceId: {}", serviceCode, traceId);
                } catch (Exception e) {
                    log.error("Failed to save service request audit log", e);
                }
            }
        }
    }

    private void executeStep(WorkflowStep step, Map<String, Object> ctx, WorkflowStepDefinition def) throws Exception {
        int maxRetries = def.isRetry() ? 3 : 1;
        int attempts = 0;
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

    private boolean shouldRecord(String serviceCode) {
        if (!recordRequests) {
            return false;
        }
        if (recordWorkflowsConfig == null || recordWorkflowsConfig.trim().isEmpty()) {
            return false;
        }
        return Arrays.stream(recordWorkflowsConfig.split(","))
                .map(String::trim)
                .anyMatch(code -> code.equalsIgnoreCase(serviceCode));
    }

    private String getCleanRequestPayload(Map<String, Object> requestContext) {
        try {
            Map<String, Object> cleanMap = new HashMap<>(requestContext);
            cleanMap.remove("requestPayload");
            cleanMap.remove("password");
            cleanMap.remove("newPassword");
            cleanMap.remove("token");
            cleanMap.remove("resetToken");
            cleanMap.remove("serviceCode");
            cleanMap.remove("traceId");
            return objectMapper.writeValueAsString(cleanMap);
        } catch (Exception e) {
            log.warn("Failed to serialize request payload", e);
            return "{}";
        }
    }

    /**
     * Executes the workflow and automatically formats the HTTP controller response.
     */
    public ResponseEntity<?> executeAndResponse(
            String serviceCode,
            Map<String, Object> context,
            String lang,
            HttpStatus successStatus,
            Class<?> successDtoClass,
            String successMessage) {

        this.executeWorkflowByServiceCode(serviceCode, context, lang);

        String status = (String) context.get("status");
        if ("SUCCESS".equals(status)) {
            Object body = objectMapper.convertValue(context, successDtoClass);
            if (successMessage != null && body instanceof GenericActionResponseDto genericDto) {
                body = new GenericActionResponseDto(genericDto.traceId(), genericDto.status(), successMessage, null);
            }
            return ResponseEntity.status(successStatus).body(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            HttpStatus failureStatus = "USER_LOGIN".equals(serviceCode) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(failureStatus).body(errorResponse);
        }
    }
}