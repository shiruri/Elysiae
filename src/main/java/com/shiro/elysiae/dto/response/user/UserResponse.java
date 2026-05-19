package com.shiro.elysiae.dto.response.user;

import com.shiro.elysiae.model.enums.Role;

public record UserResponse(
        long id,
        String username,
        Role role,
        boolean mustChangePassword) {
}
