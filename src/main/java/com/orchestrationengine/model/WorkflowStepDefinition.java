package com.orchestrationengine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowStepDefinition {
    private String id;
    private String name;
    private boolean retry;
    private boolean async;
    private boolean rollback;
    private long timeout;
    private boolean enabled;
}