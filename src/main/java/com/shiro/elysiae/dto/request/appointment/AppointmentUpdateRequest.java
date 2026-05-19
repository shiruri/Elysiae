package com.shiro.elysiae.dto.request.appointment;

import com.shiro.elysiae.model.enums.AppointmentType;

import java.time.LocalDateTime;

public record AppointmentUpdateRequest(
       AppointmentType type,
        LocalDateTime appointmentDate,
        String notes
) {
}
