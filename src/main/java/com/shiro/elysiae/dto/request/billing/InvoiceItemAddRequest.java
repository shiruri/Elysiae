package com.shiro.elysiae.dto.request.billing;

import com.shiro.elysiae.model.enums.InvoiceItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
public record InvoiceItemAddRequest(
        String description,
        InvoiceItemCategory category,
        Integer quantity,
        BigDecimal unitPrice,
        Long medicineId,
        Long labRequestId,
        Long admissionId
) {}