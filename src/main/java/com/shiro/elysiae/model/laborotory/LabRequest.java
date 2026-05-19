package com.shiro.elysiae.model.laborotory;


import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import com.shiro.elysiae.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lab_requests",
        indexes = {
                @Index(name = "idx_lab_req_status_priority", columnList = "status, priority"),
                @Index(name = "idx_lab_req_patient",         columnList = "patient_id, requested_at"),
                @Index(name = "idx_lab_req_doctor",          columnList = "doctor_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @Column(name = "test_type", nullable = false, length = 150)
    private String testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", columnDefinition = "ENUM('ROUTINE','URGENT','STAT')")
    @Builder.Default
    private LabPriority priority = LabPriority.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PENDING','IN_PROGRESS','COMPLETED')")
    @Builder.Default
    private LabRequestStatus status = LabRequestStatus.PENDING;

    @Column(name = "requested_at")
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();
}
