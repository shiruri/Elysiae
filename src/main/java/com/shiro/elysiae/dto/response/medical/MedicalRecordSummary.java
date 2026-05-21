package com.shiro.elysiae.dto.response.medical;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;

import java.time.LocalDateTime;

public record MedicalRecordSummary(
        Long id,
        String patientFullName,
        String diagnosis,
        String notes,
        LocalDateTime recordDate,
        DoctorSummary doctor,
        Long appointmentId,
        Long admissionId
) {}