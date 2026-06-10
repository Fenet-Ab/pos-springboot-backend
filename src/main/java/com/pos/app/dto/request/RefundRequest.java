package com.pos.app.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    /** Optional. Full refund if omitted. */
    private BigDecimal amount;

    /** Optional reason for the refund */
    private String reason;
}
