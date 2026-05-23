package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDetails(
        Long id,
        Long invoiceId,
        BigDecimal amount,
        PaymentMethod method,
        String receivedBy,
        LocalDateTime paidAt
) {}