package com.pos.app.dto.response;


import com.pos.app.model.enums.PaymentMethod;
import com.pos.app.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {

    private Long saleId;

    private String cashier;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private BigDecimal totalAmount;

    private String txRef;

    private String checkoutUrl;

    private BigDecimal refundAmount;

    private String refundReason;

    private String refundRefId;

    private LocalDateTime refundedAt;

    private String refundedBy;

    private LocalDateTime createdAt;

    private List<SaleItemResponse> items;
}