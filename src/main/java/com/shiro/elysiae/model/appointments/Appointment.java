package com.shiro.elysiae.model.appointments;

import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AppointmentStatus;
import com.shiro.elysiae.model.enums.AppointmentType;
import com.shiro.elysiae.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments",
        indexes = {
                @Index(name = "idx_appt_patient_dt", columnList = "patient_id, appointment_dt"),
                @Index(name = "idx_appt_doctor_dt_status", columnList = "doctor_id, appointment_dt, status"),
                @Index(name = "idx_appt_dt_status", columnList = "appointment_dt, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // RELATIONSHIPS
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;


    // =========================================================
    // APPOINTMENT DATA
    // =========================================================

    @Column(name = "appointment_dt", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;


    // =========================================================
    // AUDIT
    // =========================================================

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;




    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}