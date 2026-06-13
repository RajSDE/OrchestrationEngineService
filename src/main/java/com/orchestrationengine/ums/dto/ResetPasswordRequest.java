package com.orchestrationengine.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Reset password payload using reset token")
public record ResetPasswordRequest(
    @NotBlank(message = "Token is required")
    @Schema(description = "Unique reset token received via notification/email", example = "a2f6d0f9-2b47-4978-831d-b87569b3dfaa", required = true)
    String token,

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "New password credentials", example = "NewSuperSecretPassword456!", required = true)
    String newPassword
) {}
