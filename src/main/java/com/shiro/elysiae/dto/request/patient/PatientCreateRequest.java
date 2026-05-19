package com.shiro.elysiae.dto.request.patient;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PatientCreateRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 80, message = "Username must be between {min} and {max} characters")
        String username,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed {max} characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed {max} characters")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        String gender,

        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type")
        String bloodType,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 120)
        String email,

        String address,
        String emergencyContactName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid emergency contact phone")
        String emergencyContactPhone
) {}