package com.shiro.elysiae.model.billing;

import com.shiro.elysiae.model.enums.InvoiceItemCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "invoice_items",
        indexes = {
                @Index(name = "idx_inv_items_invoice",  columnList = "invoice_id"),
                @Index(name = "idx_inv_items_category", columnList = "category")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", columnDefinition = "ENUM('CONSULTATION','LAB','MEDICINE','BED','PROCEDURE')")
    private InvoiceItemCategory category;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}