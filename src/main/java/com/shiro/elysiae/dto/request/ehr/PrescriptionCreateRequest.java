package com.shiro.elysiae.dto.request.ehr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrescriptionCreateRequest(

        @NotNull(message = "Medical record ID is required")
        Long recordId,

        @NotBlank(message = "Medicine name is required")
        @Size(max = 200, message = "Medicine name must not exceed 200 characters")
        String medicineName,

        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage,

        @Size(max = 100, message = "Frequency must not exceed 100 characters")
        String frequency,

        Integer durationDays

) {}