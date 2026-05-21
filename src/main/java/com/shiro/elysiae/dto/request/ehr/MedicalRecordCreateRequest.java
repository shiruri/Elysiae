package com.shiro.elysiae.dto.request.ehr;

import jakarta.validation.constraints.NotNull;

public record MedicalRecordCreateRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        Long appointmentId,
        Long admissionId,

        String diagnosis,
        String notes

) {}