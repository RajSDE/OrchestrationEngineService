package com.orchestrationengine.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User authentication login payload")
public record UserLoginRequest(
    @NotBlank(message = "Username is required")
    @Schema(description = "Registered username", example = "john_doe", required = true)
    String username,

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "SuperSecretPassword123!", required = true)
    String password
) {}
