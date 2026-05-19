package com.shiro.elysiae.dto.request.audit;

import com.shiro.elysiae.model.enums.AuditAction;

import java.time.LocalDateTime;

public record AuditRequest(
        Long userId,
        AuditAction action,
        LocalDateTime from,
        LocalDateTime to
) {
}
