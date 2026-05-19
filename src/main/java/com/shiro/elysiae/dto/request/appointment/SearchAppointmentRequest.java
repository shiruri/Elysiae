package com.shiro.elysiae.dto.request.appointment;

import com.shiro.elysiae.model.enums.AppointmentType;

import java.time.LocalDateTime;

public record SearchAppointmentRequest(
        Long patientId,
        Long doctorId,
        LocalDateTime from,
        LocalDateTime to,
        AppointmentType type

) {
}
