package com.orchestrationengine.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API error response envelope.
 */
public record ErrorResponseDto(
    String traceId,
    String status,
    LocalDateTime timestamp,
    Map<String, Object> error
) {}
