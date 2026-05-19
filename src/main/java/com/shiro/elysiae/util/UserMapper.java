package com.shiro.elysiae.util;


import com.shiro.elysiae.dto.request.user.UserLoginRequest;
import com.shiro.elysiae.dto.response.user.UserCreationResponse;
import com.shiro.elysiae.dto.response.user.UserResponse;
import com.shiro.elysiae.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserLoginRequest  userLoginRequest);

    UserResponse toDto(User user);

    UserCreationResponse toCreationResponse(User user);
}
