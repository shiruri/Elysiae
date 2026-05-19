package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.patient.PatientDetails;
import com.shiro.elysiae.dto.response.patient.PatientSummary;
import com.shiro.elysiae.model.patient.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper  {

    Patient fromSummary(PatientSummary patientSummary);

    PatientSummary toSummary(Patient patients);

    @Mapping(target = "temporaryPassword", source = "user.tempPassword")
    @Mapping(target = "createdAt", expression = "java(patient.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())")

    PatientDetails toDetails(Patient patient);

}
