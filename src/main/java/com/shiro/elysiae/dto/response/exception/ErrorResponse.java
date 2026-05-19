package com.shiro.elysiae.dto.response.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant now,
        int statusCode,
        String message,
        Map<String,String> errors
) {
}
