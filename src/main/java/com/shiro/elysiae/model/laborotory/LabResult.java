package com.shiro.elysiae.model.laborotory;


import com.shiro.elysiae.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lab_results",
        indexes = {
                @Index(name = "idx_lab_res_request",  columnList = "request_id"),
                @Index(name = "idx_lab_res_abnormal", columnList = "is_abnormal")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    private LabRequest labRequest;

    @Column(name = "result_value", nullable = false, columnDefinition = "TEXT")
    private String resultValue;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(name = "is_abnormal")
    @Builder.Default
    private Boolean isAbnormal = false;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", referencedColumnName = "id")
    private User performedBy;

    @Column(name = "performed_at")
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();
}

