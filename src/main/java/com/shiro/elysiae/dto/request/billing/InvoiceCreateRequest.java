package com.shiro.elysiae.dto.request.billing;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InvoiceCreateRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        Long admissionId,

        @FutureOrPresent(message = "Due date must be today or in the future")
        LocalDate dueDate

) {}