package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSummary(
        Long id,
        BigDecimal amount,
        PaymentMethod method,
        LocalDateTime paidAt
) {}