package com.shiro.elysiae.dto.response.pharmacy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DispenseLogDetails(

        Long id,

        Long prescriptionId,
        String medicineName,
        String dosage,
        String frequency,
        Integer durationDays,

        Long medicineId,
        String medicineGenericName,
        BigDecimal unitPrice,
        Integer quantityDispensed,

        String dispensedBy,
        LocalDateTime dispensedAt

) {}