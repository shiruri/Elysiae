package com.shiro.elysiae.dto.request.billing;

import com.shiro.elysiae.model.enums.RateType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ServiceRateUpdateRequest(
        @NotNull(message = "Patient ID is required")
        Long id,
        RateType type,
        @NotNull
        BigDecimal rate,
        String description,
        boolean isActive

)
         {
}
