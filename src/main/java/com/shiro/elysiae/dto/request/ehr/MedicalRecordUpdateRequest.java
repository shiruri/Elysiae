package com.shiro.elysiae.dto.request.ehr;

import jakarta.validation.constraints.NotNull;

public record MedicalRecordUpdateRequest(
        @NotNull(message = "Medical Record ID is required")
        Long recordId,
        String diagnosis,
        String notes
) {
}
