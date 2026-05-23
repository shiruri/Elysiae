package com.shiro.elysiae.dto.request.billing;

import com.shiro.elysiae.model.enums.InvoiceStatus;

public record InvoiceSearchRequest(
        Long patientId,
        InvoiceStatus status
) {}