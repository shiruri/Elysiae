package com.shiro.elysiae.dto.request.appointment;

import com.shiro.elysiae.model.enums.AppointmentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @Positive(message = "Patient ID must be positive")
        long patientId,
        @Positive(message = "Patient ID must be positive")
        long doctorId,
        @FutureOrPresent(message = "Appointment date must be now or in the future")
        LocalDateTime appointmentDateTime,
        AppointmentType type
) {
}
