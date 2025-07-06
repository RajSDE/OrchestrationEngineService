package com.orchestrationengine.service;

import java.util.Map;

public interface WorkflowStep {
    void execute(Map<String, Object> context) throws Exception;

    default void rollback(Map<String, Object> context) {
        // Optional: override in steps that support rollback
    }
}
