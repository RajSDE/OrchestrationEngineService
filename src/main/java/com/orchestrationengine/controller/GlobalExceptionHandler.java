package com.orchestrationengine.controller;

import com.orchestrationengine.annotation.WorkflowService;
import com.orchestrationengine.config.MdcFilter;
import com.orchestrationengine.dto.GenericActionResponseDto;
import com.orchestrationengine.model.MessageCode;
import com.orchestrationengine.repository.MessageCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Global Exception Handler to format validation and parameter mismatch errors consistently.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageCodeRepository messageCodeRepository;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericActionResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HandlerMethod handlerMethod) {
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

        // Resolve language and serviceCode to translate validation message from DB
        String acceptLanguage = request.getHeader("Accept-Language");
        String resolvedLang = "en";
        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            String[] parts = acceptLanguage.split("[,;]");
            if (parts.length > 0) {
                String first = parts[0].trim();
                if (first.length() >= 2) {
                    resolvedLang = first.substring(0, 2).toLowerCase();
                }
            }
        }

        String serviceCode = null;
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null && pathVariables.containsKey("serviceCode")) {
            serviceCode = pathVariables.get("serviceCode");
        }

        if (serviceCode == null && handlerMethod != null) {
            WorkflowService ws = handlerMethod.getMethodAnnotation(WorkflowService.class);
            if (ws != null) {
                serviceCode = ws.value();
            }
        }

        String finalMessage = "Validation failed";
        if (serviceCode != null) {
            Optional<MessageCode> msgOpt = messageCodeRepository.findByServiceCodeAndCodeAndLanguage(serviceCode, "INVALID_INPUT", resolvedLang);
            if (msgOpt.isEmpty() && !"en".equals(resolvedLang)) {
                msgOpt = messageCodeRepository.findByServiceCodeAndCodeAndLanguage(serviceCode, "INVALID_INPUT", "en");
            }
            if (msgOpt.isPresent()) {
                finalMessage = msgOpt.get().getMessage();
            }
        }

        GenericActionResponseDto responseBody = new GenericActionResponseDto(
                traceId,
                "FAILED",
                finalMessage,
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

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<GenericActionResponseDto> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("component", "VALIDATION");
        errorMap.put("code", "INVALID_INPUT");
        errorMap.put("message", "Malformed JSON request body or missing parameter");

        GenericActionResponseDto responseBody = new GenericActionResponseDto(
                traceId,
                "FAILED",
                "Malformed JSON request body",
                errorMap
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericActionResponseDto> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred. Please try again later.";

        Throwable cause = ex;
        while (cause != null) {
            org.springframework.web.bind.annotation.ResponseStatus responseStatus = 
                org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(
                    cause.getClass(), org.springframework.web.bind.annotation.ResponseStatus.class);
            if (responseStatus != null) {
                status = responseStatus.value();
                message = responseStatus.reason().isEmpty() ? cause.getMessage() : responseStatus.reason();
                break;
            } else if (cause instanceof org.springframework.web.server.ResponseStatusException rse) {
                status = HttpStatus.valueOf(rse.getStatusCode().value());
                message = rse.getReason();
                break;
            } else if (cause instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
                status = HttpStatus.METHOD_NOT_ALLOWED;
                message = cause.getMessage();
                break;
            } else if (cause instanceof org.springframework.web.HttpMediaTypeNotSupportedException) {
                status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                message = cause.getMessage();
                break;
            } else if (cause instanceof org.springframework.web.bind.MissingServletRequestParameterException) {
                status = HttpStatus.BAD_REQUEST;
                message = cause.getMessage();
                break;
            }
            cause = cause.getCause();
        }

        String traceId = MDC.get(MdcFilter.MDC_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("component", "SYSTEM");
        errorMap.put("code", status.is5xxServerError() ? "INTERNAL_SYSTEM_ERROR" : "BAD_REQUEST");
        errorMap.put("message", message);

        GenericActionResponseDto responseBody = new GenericActionResponseDto(
                traceId,
                "FAILED",
                message,
                errorMap
        );

        return ResponseEntity.status(status).body(responseBody);
    }
}
