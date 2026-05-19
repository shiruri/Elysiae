package com.shiro.elysiae.dto.response.auth;


import com.shiro.elysiae.dto.response.user.UserResponse;

public record AuthResponse(
        UserResponse user,
        String token
) {
}
