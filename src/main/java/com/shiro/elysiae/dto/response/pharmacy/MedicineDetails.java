package com.shiro.elysiae.dto.response.pharmacy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MedicineDetails(

        Long id,
        String name,
        String genericName,
        String category,
        Integer stockQuantity,
        Integer reorderLevel,
        Boolean lowStock,
        BigDecimal unitPrice,
        LocalDate expiryDate

) {}