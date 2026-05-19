package com.shiro.elysiae.model.wardsbedsadmission;

import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admissions",
        indexes = {
                @Index(name = "idx_admissions_patient_status",  columnList = "patient_id, status"),
                @Index(name = "idx_admissions_doctor",          columnList = "admitting_doctor"),
                @Index(name = "idx_admissions_bed_status",      columnList = "bed_id, status"),
                @Index(name = "idx_admissions_admitted_at",     columnList = "admitted_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", referencedColumnName = "id")
    private Bed bed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitting_doctor", referencedColumnName = "id")
    private Doctor admittingDoctor;

    @Column(name = "admitted_at", nullable = false)
    private LocalDateTime admittedAt;

    @Column(name = "discharged_at")
    private LocalDateTime dischargedAt;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ADMITTED','DISCHARGED','TRANSFERRED')")
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.ADMITTED;
}