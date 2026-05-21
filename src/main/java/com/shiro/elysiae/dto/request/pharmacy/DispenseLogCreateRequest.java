package com.shiro.elysiae.dto.request.pharmacy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DispenseLogCreateRequest(

        @NotNull(message = "Prescription ID is required")
        Long prescriptionId,

        @NotNull(message = "Medicine ID is required")
        Long medicineId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity

) {}