package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.dto.response.medical.MedicalRecordDetails;
import com.shiro.elysiae.dto.response.medical.MedicalRecordSummary;
import com.shiro.elysiae.dto.response.prescription.PrescriptionSummary;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.MedicalRecord;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Prescription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "patientFullName", expression = "java(medicalRecord.getPatient().getFirstName() + \" \" + medicalRecord.getPatient().getLastName())")
    @Mapping(target = "doctor",        source = "doctor")
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "admissionId",   source = "admission.id")
    MedicalRecordSummary toRecordSummary(MedicalRecord medicalRecord);

    @Mapping(target = "patientId",       source = "patient.id")
    @Mapping(target = "patientFullName", expression = "java(medicalRecord.getPatient().getFirstName() + \" \" + medicalRecord.getPatient().getLastName())")
    @Mapping(target = "doctorId",        source = "doctor.id")
    @Mapping(target = "doctorFullName",  expression = "java(\"Dr. \" + medicalRecord.getDoctor().getFirstName() + \" \" + medicalRecord.getDoctor().getLastName())")
    @Mapping(target = "appointmentId",   source = "appointment.id")
    @Mapping(target = "admissionId",     source = "admission.id")
    @Mapping(target = "prescriptions",   source = "prescriptions")
    MedicalRecordDetails toDetails(MedicalRecord medicalRecord);

    // DoctorSummary — maps firstName/lastName directly, no expression needed
    @Mapping(target = "firstName",      source = "firstName")
    @Mapping(target = "lastName",       source = "lastName")
    @Mapping(target = "specialization", source = "specialization")
    DoctorSummary toDoctorSummary(Doctor doctor);

    PrescriptionSummary toPrescriptionSummary(Prescription prescription);
}