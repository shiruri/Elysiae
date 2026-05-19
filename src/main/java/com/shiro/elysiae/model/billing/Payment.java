package com.shiro.elysiae.model.billing;


import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_pay_invoice",      columnList = "invoice_id"),
                @Index(name = "idx_pay_method_date",  columnList = "method, paid_at"),
                @Index(name = "idx_pay_received_by",  columnList = "received_by")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", columnDefinition = "ENUM('CASH','CARD','INSURANCE','GCASH')")
    private PaymentMethod method;

    @Column(name = "paid_at")
    @Builder.Default
    private LocalDateTime paidAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", referencedColumnName = "id")
    private User receivedBy;
}
