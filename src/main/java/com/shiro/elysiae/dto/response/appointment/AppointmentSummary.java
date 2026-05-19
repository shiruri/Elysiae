package com.shiro.elysiae.dto.response.appointment;

import com.shiro.elysiae.model.enums.AppointmentStatus;
import com.shiro.elysiae.model.enums.AppointmentType;

import java.time.LocalDateTime;

public record AppointmentSummary(
        Long id,
        Long patientId,
        String patientFullName,
        Long doctorId,
        String doctorFullName,
        LocalDateTime appointmentDateTime,
        AppointmentType type,
        AppointmentStatus status
) {}
