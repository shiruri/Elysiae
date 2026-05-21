package com.shiro.elysiae.dto.request.wardsandbed;

import jakarta.validation.constraints.NotNull;

public record AdmissionTransferRequest(
        @NotNull
        Long patientId,
        @NotNull
        Long newBedId,
        Long newDoctorId
){}
