package com.shiro.elysiae.dto.response.wardsandbeds;

import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;

import java.time.LocalDateTime;

public record AdmissionDetails(

        Long id,

        // Patient
        Long patientId,
        String patientFullName,

        // Bed & Ward
        Long bedId,
        String bedCode,
        String wardName,

        // Doctor
        Long admittingDoctorId,
        String admittingDoctorFullName,
        String admittingDoctorSpecialization,

        // Admission info
        LocalDateTime admittedAt,
        LocalDateTime dischargedAt,
        String diagnosis,
        AdmissionStatus status

) {
    public static AdmissionDetails from(Admission admission) {
        return new AdmissionDetails(
                admission.getId(),

                admission.getPatient().getId(),
                admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName(),

                admission.getBed().getId(),
                admission.getBed().getBedNo(),
                admission.getBed().getWard().getName(),

                admission.getAdmittingDoctor().getId(),
                "Dr. " + admission.getAdmittingDoctor().getFirstName() + " " + admission.getAdmittingDoctor().getLastName(),
                admission.getAdmittingDoctor().getSpecialization(),

                admission.getAdmittedAt(),
                admission.getDischargedAt(),
                admission.getDiagnosis(),
                admission.getStatus()
        );
    }
}