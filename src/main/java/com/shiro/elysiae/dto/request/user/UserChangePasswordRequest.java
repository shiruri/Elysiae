package com.shiro.elysiae.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserChangePasswordRequest(
        @NotBlank
        @Size(max = 255)
        String oldPassword,
        @NotBlank
        @Size(max = 255)
        String newPassword
) {
}
