package com.shiro.elysiae.model.ehrnprescriptionsnvitals;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "prescriptions",
        indexes = {
                @Index(name = "idx_rx_record",    columnList = "record_id"),
                @Index(name = "idx_rx_dispensed", columnList = "dispensed")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", referencedColumnName = "id")
    private MedicalRecord medicalRecord;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "dispensed")
    @Builder.Default
    private Boolean dispensed = false;
}
