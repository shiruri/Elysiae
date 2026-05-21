package com.shiro.elysiae.dto.request.ehr;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VitalsCreateRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        BigDecimal temperature,
        String bloodPressure,
        Integer heartRate,
        BigDecimal oxygenSat,
        BigDecimal weightKg,
        BigDecimal heightCm

) {}