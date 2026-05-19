package com.shiro.elysiae.model.ehrnprescriptionsnvitals;


import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "medical_records",
        indexes = {
                @Index(name = "idx_records_patient_date", columnList = "patient_id, record_date"),
                @Index(name = "idx_records_doctor", columnList = "doctor_id"),
                @Index(name = "idx_records_appointment", columnList = "appointment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private Appointment appointment;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "record_date")
    @Builder.Default
    private LocalDateTime recordDate = LocalDateTime.now();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;
}
