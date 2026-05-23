package com.shiro.elysiae.model.billing;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "service_rates",
        indexes = {
                @Index(name = "idx_service_rates_key", columnList = "service_key")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_key", nullable = false, unique = true, length = 100)
    private String serviceKey;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}