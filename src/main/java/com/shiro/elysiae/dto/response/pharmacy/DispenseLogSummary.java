package com.shiro.elysiae.dto.response.pharmacy;

import java.time.LocalDateTime;

public record DispenseLogSummary(

        Long id,
        String medicineName,
        Integer quantity,
        String dispensedBy,
        LocalDateTime dispensedAt

) {}