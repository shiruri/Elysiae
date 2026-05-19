package com.shiro.elysiae.model.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user",         columnList = "user_id"),
                @Index(name = "idx_audit_action",        columnList = "action"),
                @Index(name = "idx_audit_performed_at",  columnList = "performed_at"),
                @Index(name = "idx_audit_entity",        columnList = "entity, entity_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "performed_at")
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();
}