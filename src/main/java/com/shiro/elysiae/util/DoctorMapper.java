package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface DoctorMapper {

    DoctorSummary toSummary(Doctor doctor);
}
