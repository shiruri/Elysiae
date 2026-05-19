package com.shiro.elysiae.dto.response.appointment;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.dto.response.patient.PatientSummary;
import com.shiro.elysiae.model.enums.AppointmentStatus;
import com.shiro.elysiae.model.enums.AppointmentType;

import java.time.LocalDateTime;

public record AppointmentDetails(
        Long id,
        PatientSummary patient,
        DoctorSummary doctor,
        LocalDateTime appointmentDateTime,
        AppointmentType type,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt
) {}