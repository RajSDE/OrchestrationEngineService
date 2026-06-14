package com.orchestrationengine.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public record UserRegistrationRequest(
    @Pattern(regexp = "^\\s*$|^.{3,}$", message = "Username must be at least 3 characters long")
    @Schema(description = "Unique username for the profile", example = "john_doe", required = false)
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Schema(description = "Primary contact email address", example = "john.doe@example.com", required = true)
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "Password credentials", example = "SuperSecretPassword123!", required = true)
    String password,

    @Schema(description = "First name of the user", example = "John")
    String firstName,

    @Schema(description = "Last name of the user", example = "Doe")
    String lastName,

    @Schema(description = "Preferred language locale (ISO 2-letter)", example = "en", defaultValue = "en")
    String preferredLanguage,

    @Schema(description = "Gender designation", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    String gender,

    @Schema(description = "Contact phone number (international format)", example = "+1234567890")
    String mobileNumber
) {}
