package com.shiro.elysiae.model.wardsbedsadmission;


import com.shiro.elysiae.model.enums.WardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "wards",
        indexes = {
                @Index(name = "idx_wards_type", columnList = "type")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('GENERAL','ICU','PEDIATRIC','MATERNITY','SURGICAL')")
    private WardType type;

    @Column(name = "floor", length = 20)
    private String floor;

    @OneToMany(mappedBy = "ward", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bed> beds;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}