package com.shiro.elysiae.dto.response.wardsandbeds;

import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;

import java.time.LocalDateTime;

public record AdmissionSummary(

        Long id,
        Long patientId,
        String patientFullName,
        String admittingDoctorFullName,
        LocalDateTime admittedAt,
        LocalDateTime dischargedAt,
        AdmissionStatus status

) {
    public static AdmissionSummary from(Admission admission) {
        return new AdmissionSummary(
                admission.getId(),
                admission.getPatient().getId(),
                admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName(),
                "Dr. " + admission.getAdmittingDoctor().getFirstName() + " " + admission.getAdmittingDoctor().getLastName(),
                admission.getAdmittedAt(),
                admission.getDischargedAt(),
                admission.getStatus()
        );
    }
}