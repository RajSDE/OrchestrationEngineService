package com.orchestrationengine.ums.dto;

import java.util.UUID;

/**
 * Login API response DTO carrying JWT tokens.
 */
public record UserLoginResponseDto(
    String traceId,
    String status,
    String message,
    String accessToken,
    String refreshToken,
    UUID userProfileId
) {}
