package com.shiro.elysiae.dto.response.audit;

import com.shiro.elysiae.model.enums.AuditAction;

public record AuditResponse(
        long userid,
        AuditAction action
) {
}
