package com.pos.app.model.entity;



import com.pos.app.model.enums.PaymentMethod;
import com.pos.app.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String txRef;

    private String checkoutUrl;

    private BigDecimal refundAmount;

    private String refundReason;

    private String refundRefId;

    private LocalDateTime refundedAt;

    @ManyToOne
    @JoinColumn(name = "refunded_by_id")
    private User refundedBy;

    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @OneToMany(
            mappedBy = "sale",
            cascade = CascadeType.ALL
    )
    private List<SaleItem> items;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}