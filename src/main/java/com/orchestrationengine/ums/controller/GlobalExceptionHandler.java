package com.orchestrationengine.ums.controller;

import com.orchestrationengine.config.MdcFilter;
import com.orchestrationengine.ums.dto.GenericActionResponseDto;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global Exception Handler to format validation and parameter mismatch errors consistently.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericActionResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("component", "VALIDATION");
        errorMap.put("code", "INVALID_INPUT");
        errorMap.put("details", details);

        GenericActionResponseDto responseBody = new GenericActionResponseDto(
                traceId,
                "FAILED",
                "Validation failed",
                errorMap
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericActionResponseDto> handleTypeMismatchExceptions(MethodArgumentTypeMismatchException ex) {
        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("component", "VALIDATION");
        errorMap.put("code", "INVALID_TYPE");
        errorMap.put("message", "Invalid parameter: " + ex.getName() + " (expected type: " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown") + ")");

        GenericActionResponseDto responseBody = new GenericActionResponseDto(
                traceId,
                "FAILED",
                "Type conversion failed",
                errorMap
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
