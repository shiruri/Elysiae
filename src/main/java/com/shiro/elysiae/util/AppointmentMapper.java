package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.appointment.AppointmentDetails;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.model.appointments.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "createdAt", expression = "java(appointment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())")
    AppointmentDetails toDetails(Appointment appointment);

    @Mapping(target = "patientId",       source = "patient.id")
    @Mapping(target = "patientFullName", expression = "java(appointment.getPatient().getFirstName() + \" \" + appointment.getPatient().getLastName())")
    @Mapping(target = "doctorId",        source = "doctor.id")
    @Mapping(target = "doctorFullName",  expression = "java(appointment.getDoctor().getFirstName() + \" \" + appointment.getDoctor().getLastName())")
    AppointmentSummary toSummary(Appointment appointment);

    List<AppointmentSummary> toSummaryList(List<Appointment> appointments);
}