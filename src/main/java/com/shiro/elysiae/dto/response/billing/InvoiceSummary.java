package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceSummary(
        Long id,
        Long patientId,
        String patientFullName,
        Long admissionId,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingBalance,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt
) {}