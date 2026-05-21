package com.shiro.elysiae.dto.response.medical;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalsDetails(

        Long id,
        Long patientId,
        String patientFullName,
        String recordedBy,
        LocalDateTime recordedAt,

        BigDecimal temperature,
        String bloodPressure,
        Integer heartRate,
        BigDecimal oxygenSat,
        BigDecimal weightKg,
        BigDecimal heightCm

) {}