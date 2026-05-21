package com.shiro.elysiae.dto.response.medical;

import com.shiro.elysiae.dto.response.prescription.PrescriptionSummary;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalRecordDetails(
        Long id,

        Long patientId,
        String patientFullName,

        Long doctorId,
        String doctorFullName,

        Long appointmentId,
        Long admissionId,

        String diagnosis,
        String notes,
        LocalDateTime recordDate,

        List<PrescriptionSummary> prescriptions
) {}