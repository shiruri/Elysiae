package com.shiro.elysiae.dto.request.laborotory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LabResultCreateRequest(

        @NotNull(message = "Lab request ID is required")
        Long labRequestId,

        @NotBlank(message = "Result value is required")
        String resultValue,

        String normalRange,

        Boolean isAbnormal,

        String remarks

) {}