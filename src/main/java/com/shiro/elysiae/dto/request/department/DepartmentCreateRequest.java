package com.shiro.elysiae.dto.request.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Department name must not exceed 100 characters")
        String name,

        @Size(max = 20, message = "Floor must not exceed 20 characters")
        String floor
) {
}