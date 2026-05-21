package com.shiro.elysiae.dto.request.wardsandbed;

import com.shiro.elysiae.model.enums.WardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WardCreateRequest(

        @NotBlank(message = "Ward name is required")
        @Size(max = 100, message = "Ward name must not exceed 100 characters")
        String name,

        @NotNull(message = "Ward type is required")
        WardType type,

        @Size(max = 20, message = "Floor must not exceed 20 characters")
        String floor

) {
}
