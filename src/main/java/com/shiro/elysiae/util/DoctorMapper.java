package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.doctor.DoctorDetails;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    DoctorSummary toSummary(Doctor doctor);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    DoctorDetails toDetails(Doctor doctor);
}
