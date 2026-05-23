package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.user.*;
import com.shiro.elysiae.dto.response.auth.AuthResponse;
import com.shiro.elysiae.dto.response.user.UserCreationResponse;
import com.shiro.elysiae.dto.response.user.UserResponse;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.repository.UserRepository;
import com.shiro.elysiae.util.JwtUtils;
import com.shiro.elysiae.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final AuditService auditService;
    @Transactional
    public AuthResponse loginUser(UserLoginRequest  userLoginRequest) {
        if(userLoginRequest.password().isBlank() || userLoginRequest.username().isBlank()) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        Optional<User> user = Optional.of(userRepository.
                findByUsername(userLoginRequest.username()).orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_FOUND)
                ));

        if(!passwordEncoder.matches(userLoginRequest.password(), user.get().getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        auditService.log(String.valueOf(AuditAction.USER_LOGIN),user.get().getRole().name(),user.get().getId());

        return new AuthResponse(userMapper.toDto(user.get()),generateToken(user.get()));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        Authentication auth = getAuthentication();
        if(auth == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        long userId = Long.parseLong(auth.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return userMapper.toDto(user);
    }
    @Transactional
    public UserCreationResponse registerUser(UserCreateRequest request) {
        Authentication auth = getAuthentication();
        if(auth == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String tempPassword = request.username() + "-" + (1000 + new Random().nextInt(9000));
        String password =  passwordEncoder.encode(tempPassword);
        User user = User.builder()
                .username(request.username())
                .password(password)
                .role(request.role())
                .isActive(false)
                .build();

        if (!userRepository.findByUsername(request.username()).isEmpty()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        User saved = userRepository.save(user);

        auditService.log(String.valueOf(AuditAction.USER_CREATED),user.getRole().name(),user.getId());
        saved.setTempPassword(tempPassword);
        return userMapper.toCreationResponse(saved);
    }

    public String generateToken(User user) {
        return jwtUtils.generateToken(user.getId(),user.getRole(),user.getMustChangePassword());
    }

    @Transactional
    public UserResponse changePassword(UserChangePasswordRequest request,long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
        if (user.getDeletedAt() != null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        validate(request,id,user);
        String newPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
        auditService.log(String.valueOf(AuditAction.USER_UPDATED),user.getUsername(),user.getId());
        return userMapper.toDto(userRepository.save(user));

    }

    public Page<UserResponse> getUsers(UserSearchRequest request, Pageable pageable) {
        return userRepository.searchUsers(
                request.keyword(),
                request.role(),
                request.isActive(),
                pageable).map(userMapper::toDto);
    }

    public void deleteUser(long id) {
        validate(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
        auditService.log(AuditAction.USER_DELETED.name(), "User", id);
    }

    public UserResponse updateUser(long id, UserUpdateRequest request) {

        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if(request.username().isBlank() && request.role().name().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_UPDATE);
        }
        if(!request.username().isBlank()) {
            user.setUsername(request.username());
        }
        if(!request.role().name().isBlank()) {
            user.setRole(request.role());
        }
        auditService.log(AuditAction.USER_UPDATED.name(), user.getUsername(),user.getId());
        return userMapper.toDto(userRepository.save(user));
    }


    private void validate(UserChangePasswordRequest request,long id,User user) {
        Authentication auth = getAuthentication();

        long currentUserId = Long.parseLong(auth.getName());
        boolean isSelf  = currentUserId == id;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSelf && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if(!passwordEncoder.matches(request.oldPassword(),user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }


    private void validate(long id) {
        Authentication auth = getAuthentication();

        long currentUserId = Long.parseLong(auth.getName());
        boolean isSelf  = currentUserId == id;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if(auth == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if (!isSelf && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
    public String logoutUser(long id) {

        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        user.setIsActive(false);
        userRepository.save(user);
        return "Logged out Successfully";
    }


    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
