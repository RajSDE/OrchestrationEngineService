package com.orchestrationengine.ums.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestrationengine.annotation.WorkflowService;
import com.orchestrationengine.dto.GenericActionResponseDto;
import com.orchestrationengine.service.WorkflowExecutor;
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
    @WorkflowService("USER_REGISTRATION")
    @Operation(summary = "Register a new user", description = "Executes the USER_REGISTRATION workflow which validates data, creates profile & credentials, and schedules notification.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registration succeeded"),
        @ApiResponse(responseCode = "400", description = "Validation failed, username/email conflict, or workflow error")
    })
    public ResponseEntity<?> registerUser(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Valid @RequestBody UserRegistrationRequest request) {
        Map<String, Object> context = toContextMap(request);
        String lang = resolveLanguage(acceptLanguage, request.preferredLanguage());
        return workflowExecutor.executeAndResponse("USER_REGISTRATION", context, lang, HttpStatus.CREATED, UserRegistrationResponseDto.class, null);
    }

    @PostMapping("/login")
    @WorkflowService("USER_LOGIN")
    @Operation(summary = "User Login", description = "Executes USER_LOGIN workflow to verify credentials and generate JWT access & refresh tokens.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successful authentication and session generation"),
        @ApiResponse(responseCode = "401", description = "Authentication failed (invalid credentials or inactive account)")
    })
    public ResponseEntity<?> loginUser(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Valid @RequestBody UserLoginRequest request) {
        Map<String, Object> context = toContextMap(request);
        String lang = resolveLanguage(acceptLanguage, null);
        return workflowExecutor.executeAndResponse("USER_LOGIN", context, lang, HttpStatus.OK, UserLoginResponseDto.class, null);
    }

    @DeleteMapping("/{userId}")
    @WorkflowService("DELETE_USER")
    @Operation(summary = "Delete User Profile", description = "Executes DELETE_USER workflow to purge user profile data and security credentials.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile purged successfully"),
        @ApiResponse(responseCode = "400", description = "Purge failed (user not found or active constraints)")
    })
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @PathVariable UUID userId) {
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put("userId", userId.toString());
        String lang = resolveLanguage(acceptLanguage, null);
        return workflowExecutor.executeAndResponse("DELETE_USER", context, lang, HttpStatus.OK, GenericActionResponseDto.class, "User deleted successfully");
    }

    @PostMapping("/{userId}/deactivate")
    @WorkflowService("DEACTIVATE_USER")
    @Operation(summary = "Deactivate User", description = "Executes DEACTIVATE_USER workflow to set user profile status to inactive.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile deactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Deactivation failed")
    })
    public ResponseEntity<?> deactivateUser(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @PathVariable UUID userId) {
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put("userId", userId.toString());
        String lang = resolveLanguage(acceptLanguage, null);
        return workflowExecutor.executeAndResponse("DEACTIVATE_USER", context, lang, HttpStatus.OK, GenericActionResponseDto.class, "User deactivated successfully");
    }

    @PostMapping("/forgot-password")
    @WorkflowService("FORGOT_PASSWORD")
    @Operation(summary = "Request Password Reset", description = "Executes FORGOT_PASSWORD workflow to generate reset token and dispatch notifications.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset token generated and email dispatched successfully"),
        @ApiResponse(responseCode = "400", description = "Request failed (email not found or inactive account)")
    })
    public ResponseEntity<?> forgotPassword(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        String lang = resolveLanguage(acceptLanguage, null);
        return workflowExecutor.executeAndResponse("FORGOT_PASSWORD", context, lang, HttpStatus.OK, GenericActionResponseDto.class, "Password reset notification sent successfully");
    }

    @PostMapping("/reset-password")
    @WorkflowService("RESET_PASSWORD")
    @Operation(summary = "Reset Password credentials", description = "Executes RESET_PASSWORD workflow to verify reset token and update account password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Reset failed (invalid or expired token)")
    })
    public ResponseEntity<?> resetPassword(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        String lang = resolveLanguage(acceptLanguage, null);
        return workflowExecutor.executeAndResponse("RESET_PASSWORD", context, lang, HttpStatus.OK, GenericActionResponseDto.class, "Password updated successfully");
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

    private String resolveLanguage(String acceptLanguage, String preferredLanguage) {
        if (preferredLanguage != null && !preferredLanguage.trim().isEmpty()) {
            return preferredLanguage.trim().substring(0, 2).toLowerCase();
        }
        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            String[] parts = acceptLanguage.split("[,;]");
            if (parts.length > 0) {
                String first = parts[0].trim();
                if (first.length() >= 2) {
                    return first.substring(0, 2).toLowerCase();
                }
            }
        }
        return "en";
    }
}
