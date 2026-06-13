package com.orchestrationengine.ums.dto;

import java.util.Map;

/**
 * Generic response DTO for delete, deactivate, forgot/reset password API actions.
 */
public record GenericActionResponseDto(
    String traceId,
    String status,
    String message,
    Map<String, Object> error
) {}
