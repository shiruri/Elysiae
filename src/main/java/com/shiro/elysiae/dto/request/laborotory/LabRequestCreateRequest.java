package com.shiro.elysiae.dto.request.laborotory;

import com.shiro.elysiae.model.enums.LabPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LabRequestCreateRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        @NotBlank(message = "Test type is required")
        @Size(max = 150, message = "Test type must not exceed 150 characters")
        String testType,

        LabPriority priority  // defaults to ROUTINE if null

) {}