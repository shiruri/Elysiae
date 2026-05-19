package com.shiro.elysiae.dto.response.doctor;

import com.shiro.elysiae.dto.response.department.DepartmentSummary;

public record DoctorDetails(



        Long id,



        Long userId,
        String username,
        String email,



        DepartmentSummary department,


        String firstName,
        String lastName,
        String specialization,
        String licenseNumber,
        String phone

) {
}