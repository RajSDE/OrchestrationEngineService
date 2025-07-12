package com.orchestrationengine.exception;

import org.springframework.context.MessageSource;
import java.util.Locale;

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
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
