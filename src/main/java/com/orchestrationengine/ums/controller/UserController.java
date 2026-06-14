package com.orchestrationengine.ums.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestrationengine.service.WorkflowExecutor;
import com.orchestrationengine.dto.GenericActionResponseDto;
import com.orchestrationengine.dto.ErrorResponseDto;
import com.orchestrationengine.ums.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller to expose user operations using ObjectMapper to convert context maps directly to DTOs.
 */
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user registration, authentication, password reset, and profile management.")
@SuppressWarnings("unchecked")
public class UserController {

    private final WorkflowExecutor workflowExecutor;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Executes the USER_REGISTRATION workflow which validates data, creates profile & credentials, and schedules notification.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registration succeeded"),
        @ApiResponse(responseCode = "400", description = "Validation failed, username/email conflict, or workflow error")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        Map<String, Object> context = toContextMap(request);
        String lang = request.preferredLanguage() != null ? request.preferredLanguage() : "en";
        workflowExecutor.executeWorkflowByServiceCode("USER_REGISTRATION", context, lang);

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            UserRegistrationResponseDto body = objectMapper.convertValue(context, UserRegistrationResponseDto.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Executes USER_LOGIN workflow to verify credentials and generate JWT access & refresh tokens.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successful authentication and session generation"),
        @ApiResponse(responseCode = "401", description = "Authentication failed (invalid credentials or inactive account)")
    })
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest request) {
        Map<String, Object> context = toContextMap(request);
        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", context, "en");

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            UserLoginResponseDto body = objectMapper.convertValue(context, UserLoginResponseDto.class);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete User Profile", description = "Executes DELETE_USER workflow to purge user profile data and security credentials.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile purged successfully"),
        @ApiResponse(responseCode = "400", description = "Purge failed (user not found or active constraints)")
    })
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put("userId", userId.toString());

        workflowExecutor.executeWorkflowByServiceCode("DELETE_USER", context, "en");

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            GenericActionResponseDto body = objectMapper.convertValue(context, GenericActionResponseDto.class);
            body = new GenericActionResponseDto(body.traceId(), body.status(), "User deleted successfully", null);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate User", description = "Executes DEACTIVATE_USER workflow to set user profile status to inactive.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile deactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Deactivation failed")
    })
    public ResponseEntity<?> deactivateUser(@PathVariable UUID userId) {
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put("userId", userId.toString());

        workflowExecutor.executeWorkflowByServiceCode("DEACTIVATE_USER", context, "en");

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            GenericActionResponseDto body = objectMapper.convertValue(context, GenericActionResponseDto.class);
            body = new GenericActionResponseDto(body.traceId(), body.status(), "User deactivated successfully", null);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request Password Reset", description = "Executes FORGOT_PASSWORD workflow to generate reset token and dispatch notifications.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset token generated and email dispatched successfully"),
        @ApiResponse(responseCode = "400", description = "Request failed (email not found or inactive account)")
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        workflowExecutor.executeWorkflowByServiceCode("FORGOT_PASSWORD", context, "en");

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            GenericActionResponseDto body = objectMapper.convertValue(context, GenericActionResponseDto.class);
            body = new GenericActionResponseDto(body.traceId(), body.status(), "Password reset notification sent successfully", null);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password credentials", description = "Executes RESET_PASSWORD workflow to verify reset token and update account password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Reset failed (invalid or expired token)")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        workflowExecutor.executeWorkflowByServiceCode("RESET_PASSWORD", context, "en");

        String status = (String) context.get("status");

        if ("SUCCESS".equals(status)) {
            GenericActionResponseDto body = objectMapper.convertValue(context, GenericActionResponseDto.class);
            body = new GenericActionResponseDto(body.traceId(), body.status(), "Password updated successfully", null);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> errorMap = (Map<String, Object>) context.get("error");
            String traceId = (String) context.get("traceId");
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                traceId,
                "FAILED",
                LocalDateTime.now(),
                errorMap
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    private Map<String, Object> toContextMap(Object request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> context = new ConcurrentHashMap<>();
        map.forEach((k, v) -> {
            if (v != null) {
                context.put(k, v);
            }
        });
        context.put("requestPayload", request);
        return context;
    }
}
