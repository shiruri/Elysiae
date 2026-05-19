package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceDetails(
        Long id,
        Long patientId,
        String patientFullName,
        Long admissionId,
        List<InvoiceItemSummary> items,
        List<PaymentSummary> payments,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingBalance,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt
) {}