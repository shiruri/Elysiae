package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.medical.VitalsDetails;
import com.shiro.elysiae.dto.response.medical.VitalsSummary;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Vitals;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VitalsMapper {

    @Mapping(target = "patientId",       source = "patient.id")
    @Mapping(target = "patientFullName", expression = "java(vitals.getPatient().getFirstName() + \" \" + vitals.getPatient().getLastName())")
    @Mapping(target = "recordedBy",      source = "recordedBy")
    VitalsDetails toVitalsDetails(Vitals vitals);
    VitalsSummary ToVitalsSummary(Vitals vitals);
    default String mapUser(User user) {
        if (user == null) return null;
        return user.getUsername();
    }
}
