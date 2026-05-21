package com.shiro.elysiae.dto.response.medical;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalsSummary(

        Long id,
        LocalDateTime recordedAt,
        BigDecimal temperature,
        String bloodPressure,
        Integer heartRate,
        BigDecimal oxygenSat


) {}