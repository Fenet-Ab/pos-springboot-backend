package com.pos.app.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment method for orders")
public enum PaymentMethod {
        CASH,
        TELEBIRR,
        CBE_BIRR,
        BANK_CARD

}
