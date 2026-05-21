package com.shiro.elysiae.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MedicineCreateRequest(

        @NotBlank(message = "Medicine name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Size(max = 200, message = "Generic name must not exceed 200 characters")
        String genericName,

        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @PositiveOrZero(message = "Stock quantity must be 0 or more")
        Integer stockQuantity,

        @PositiveOrZero(message = "Reorder level must be 0 or more")
        Integer reorderLevel,

        @NotNull(message = "Unit price is required")
        BigDecimal unitPrice,

        LocalDate expiryDate

) {}