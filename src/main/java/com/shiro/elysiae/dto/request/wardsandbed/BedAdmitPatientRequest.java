package com.shiro.elysiae.dto.request.wardsandbed;

import com.shiro.elysiae.dto.request.appointment.AppointmentCreateRequest;
import com.shiro.elysiae.model.enums.AppointmentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BedAdmitPatientRequest(
        @NotNull
        Long patientId,
        @NotNull
        Long bedId,
        @NotNull
        Long doctorId
) {
}
