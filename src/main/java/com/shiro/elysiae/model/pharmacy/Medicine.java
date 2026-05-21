package com.shiro.elysiae.model.pharmacy;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "medicines",
        indexes = {
                @Index(name = "idx_med_name",         columnList = "name"),
                @Index(name = "idx_med_generic_name", columnList = "generic_name"),
                @Index(name = "idx_med_stock",        columnList = "stock_quantity, reorder_level"),
                @Index(name = "idx_med_expiry",       columnList = "expiry_date")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

