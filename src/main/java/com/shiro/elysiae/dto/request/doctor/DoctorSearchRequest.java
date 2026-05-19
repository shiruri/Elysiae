package com.shiro.elysiae.dto.request.doctor;

public record DoctorSearchRequest(
        Long doctorId,
        String departmentName,
        String specialization
) {
}
