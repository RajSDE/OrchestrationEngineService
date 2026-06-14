package com.orchestrationengine.ums.dto;

import java.util.UUID;

/**
 * Registration API response DTO.
 */
public record UserRegistrationResponseDto(
    String traceId,
    String status,
    String message,
    UUID userProfileId,
    String username,
    String email
) {}
