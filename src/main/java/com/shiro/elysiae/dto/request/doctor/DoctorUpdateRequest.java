package com.shiro.elysiae.dto.request.doctor;

import jakarta.validation.constraints.Size;

public record DoctorUpdateRequest(

        Long departmentId,

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 150, message = "Specialization must not exceed 150 characters")
        String specialization,

        @Size(max = 80, message = "License number must not exceed 80 characters")
        String licenseNumber,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone

) {}