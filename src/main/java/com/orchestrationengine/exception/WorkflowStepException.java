package com.orchestrationengine.exception;

import lombok.Getter;

@Getter
public class WorkflowStepException extends RuntimeException {
    private final String errorCode;
    private String errorMessage;

    public WorkflowStepException(String errorCode, String errorMessage) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    public WorkflowStepException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
