package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.user.*;
import com.shiro.elysiae.dto.response.auth.AuthResponse;
import com.shiro.elysiae.dto.response.user.UserCreationResponse;
import com.shiro.elysiae.dto.response.user.UserResponse;
import com.shiro.elysiae.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody UserLoginRequest
                                                  userLoginRequest) {
        return ResponseEntity.ok().body(authService.loginUser(userLoginRequest));
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok().body(authService.getCurrentUser());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> registerUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok().body(authService.registerUser(request));

    }

    @PatchMapping("/change-password/{id}")
    public ResponseEntity<UserResponse> changePassword(@Valid @RequestBody UserChangePasswordRequest request,
                                                       @PathVariable long id) {
        return ResponseEntity.ok().body(authService.changePassword(request,id));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/search-users")
    public ResponseEntity<Page<UserResponse>> getUsers(@RequestBody UserSearchRequest request,
                                                       Pageable pageable) {
        return ResponseEntity.ok().body(authService.getUsers(request,pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteByUserId(@PathVariable long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok().body(HttpStatus.OK);
    }
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateByUserId(@PathVariable long id
            ,@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok().body(authService.updateUser(id,request));
    }

}
