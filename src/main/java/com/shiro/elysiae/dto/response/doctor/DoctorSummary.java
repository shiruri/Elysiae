package com.shiro.elysiae.dto.response.doctor;

public record DoctorSummary(
        Long id,
        String firstName,
        String lastName,
        String specialization
) {}