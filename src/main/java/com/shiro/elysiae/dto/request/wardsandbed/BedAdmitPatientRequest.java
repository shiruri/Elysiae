package com.shiro.elysiae.dto.request.wardsandbed;

import jakarta.validation.constraints.NotNull;

public record BedAdmitPatientRequest(
        @NotNull
        Long patientId,
        @NotNull
        Long bedId,
        @NotNull
        Long doctorId
) {
}
