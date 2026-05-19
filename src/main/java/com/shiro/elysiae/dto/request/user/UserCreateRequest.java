package com.shiro.elysiae.dto.request.user;

import com.shiro.elysiae.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Size(min = 6, max = 100)
        String username,
        @NotNull
        Role role
) {
}
