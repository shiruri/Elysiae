package com.shiro.elysiae.model.pharmacy;


import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Prescription;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "dispense_logs",
        indexes = {
                @Index(name = "idx_disp_prescription",  columnList = "prescription_id"),
                @Index(name = "idx_disp_medicine_time", columnList = "medicine_id, dispensed_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispenseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", referencedColumnName = "id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensed_by", referencedColumnName = "id")
    private User dispensedBy;

    @Column(name = "dispensed_at")
    @Builder.Default
    private LocalDateTime dispensedAt = LocalDateTime.now();
}