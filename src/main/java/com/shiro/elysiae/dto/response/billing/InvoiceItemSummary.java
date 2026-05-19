package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.InvoiceItemCategory;

import java.math.BigDecimal;

public record InvoiceItemSummary(
        Long id,
        String description,
        InvoiceItemCategory category,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {}