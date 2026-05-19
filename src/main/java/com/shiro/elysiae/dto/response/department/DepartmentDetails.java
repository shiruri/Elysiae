package com.shiro.elysiae.dto.response.department;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;

import java.util.List;

public record DepartmentDetails(
        Long id,
        String name,
        String floor,

        List<DoctorSummary> doctors
) {
}
