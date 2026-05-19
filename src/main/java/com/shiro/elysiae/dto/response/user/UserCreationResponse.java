package com.shiro.elysiae.dto.response.user;

import com.shiro.elysiae.model.enums.Role;

public record UserCreationResponse(
        String username,
        Role role,
        String tempPassword,
        boolean mustChangePassword

) {
}
