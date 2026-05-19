package com.shiro.elysiae.dto.response.medical;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.dto.response.prescription.PrescriptionSummary;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalRecordSummary(
        Long id,
        String diagnosis,
        String notes,
        LocalDateTime recordDate,
        DoctorSummary doctor,
        List<PrescriptionSummary> prescriptions
) {}
