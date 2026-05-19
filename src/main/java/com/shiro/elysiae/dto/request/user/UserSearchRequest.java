package com.shiro.elysiae.dto.request.user;

import com.shiro.elysiae.model.enums.Role;

public record UserSearchRequest(
        String keyword,

        Role role,

        Boolean isActive
) {}