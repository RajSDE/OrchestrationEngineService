package com.orchestrationengine.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 TaskDecorator that captures the submitting thread's SLF4J MDC (mapped diagnostic context)
 and sets that context on the worker thread for the lifetime of the decorated Runnable.
 This prevents MDC values (correlation IDs, user IDs, etc.) from being lost when tasks
 are executed on thread-pool threads and ensures log statements inside the task include
 the original context. The decorator is stateless and safe to reuse (typical singleton usage).

 Note: the current implementation clears the worker thread MDC after execution to avoid
 leakage; if worker threads may have pre-existing MDC you want preserved, consider saving
 and restoring the previous context instead of clearing.

 Usage example: register with your executor via
 executor.setTaskDecorator(new MdcTaskDecorator());
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}