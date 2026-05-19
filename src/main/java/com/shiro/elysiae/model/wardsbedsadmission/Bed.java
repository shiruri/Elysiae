package com.shiro.elysiae.model.wardsbedsadmission;


import com.shiro.elysiae.model.enums.BedStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "beds",
        indexes = {
                @Index(name = "idx_beds_ward_status", columnList = "ward_id, status")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", referencedColumnName = "id")
    private Ward ward;

    @Column(name = "bed_no", nullable = false, length = 20)
    private String bedNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('AVAILABLE','OCCUPIED','MAINTENANCE')")
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;
}

