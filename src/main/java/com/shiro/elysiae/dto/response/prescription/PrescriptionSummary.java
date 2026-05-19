package com.shiro.elysiae.dto.response.prescription;

public record PrescriptionSummary(
        Long id,
        String medicineName,
        String dosage,
        String frequency,
        Integer durationDays,
        Boolean dispensed
) {}