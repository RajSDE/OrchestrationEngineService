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
        return workflowExecutor.executeAndResponse("USER_REGISTRATION", context, lang, HttpStatus.CREATED, UserRegistrationResponseDto.class, null);
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Executes USER_LOGIN workflow to verify credentials and generate JWT access & refresh tokens.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successful authentication and session generation"),
        @ApiResponse(responseCode = "401", description = "Authentication failed (invalid credentials or inactive account)")
    })
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest request) {
        Map<String, Object> context = toContextMap(request);
        return workflowExecutor.executeAndResponse("USER_LOGIN", context, "en", HttpStatus.OK, UserLoginResponseDto.class, null);
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
        return workflowExecutor.executeAndResponse("DELETE_USER", context, "en", HttpStatus.OK, GenericActionResponseDto.class, "User deleted successfully");
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
        return workflowExecutor.executeAndResponse("DEACTIVATE_USER", context, "en", HttpStatus.OK, GenericActionResponseDto.class, "User deactivated successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request Password Reset", description = "Executes FORGOT_PASSWORD workflow to generate reset token and dispatch notifications.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset token generated and email dispatched successfully"),
        @ApiResponse(responseCode = "400", description = "Request failed (email not found or inactive account)")
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        return workflowExecutor.executeAndResponse("FORGOT_PASSWORD", context, "en", HttpStatus.OK, GenericActionResponseDto.class, "Password reset notification sent successfully");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password credentials", description = "Executes RESET_PASSWORD workflow to verify reset token and update account password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Reset failed (invalid or expired token)")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> context = toContextMap(request);
        return workflowExecutor.executeAndResponse("RESET_PASSWORD", context, "en", HttpStatus.OK, GenericActionResponseDto.class, "Password updated successfully");
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
