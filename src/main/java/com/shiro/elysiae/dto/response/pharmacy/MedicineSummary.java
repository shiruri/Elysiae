package com.shiro.elysiae.dto.response.pharmacy;

import java.math.BigDecimal;

public record MedicineSummary(

        Long id,
        String name,
        String genericName,
        String category,
        Integer stockQuantity,
        Integer reorderLevel,
        Boolean lowStock,
        BigDecimal unitPrice

) {}