package com.shiro.elysiae.dto.request.user;

import com.shiro.elysiae.model.enums.Role;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 6, max = 100)
        String username,
        Role role
) {
}
