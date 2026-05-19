package com.shiro.elysiae.dto.response.patient;

import com.shiro.elysiae.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientDetails(
        Long id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String bloodType,
        String phone,
        String email,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        LocalDateTime createdAt,
        String temporaryPassword

) {}

