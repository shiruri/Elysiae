package com.shiro.elysiae.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @NotBlank
        @Size(max = 50)
        String username,
        @NotBlank
        @Size(max = 255)
        String password
) {
}
