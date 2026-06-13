package com.orchestrationengine.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Forgot password request payload")
public record ForgotPasswordRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Schema(description = "Registered email address associated with the account", example = "john.doe@example.com", required = true)
    String email
) {}
