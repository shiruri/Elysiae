package com.shiro.elysiae.dto.response.billing;

import com.shiro.elysiae.model.enums.RateType;

import java.math.BigDecimal;

public record ServiceRateDetails(
        long serviceRate,
        String serviceKey,
        RateType type,
        BigDecimal rate,
        boolean isActive
) {
}
