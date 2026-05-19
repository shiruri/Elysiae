package com.shiro.elysiae.model.ehrnprescriptionsnvitals;


import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "vitals",
        indexes = {
                @Index(name = "idx_vitals_patient_time", columnList = "patient_id, recorded_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", referencedColumnName = "id")
    private User recordedBy;

    @Column(name = "recorded_at")
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "oxygen_sat", precision = 5, scale = 2)
    private BigDecimal oxygenSat;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;
}