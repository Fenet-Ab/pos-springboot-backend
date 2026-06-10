package com.pos.app.dto.request;

import com.pos.app.model.enums.PaymentMethod;
import lombok.Data;

@Data
public class CheckoutCartRequest {

    private PaymentMethod paymentMethod;

    /** Required for TELEBIRR and CBE_BIRR */
    private String phoneNumber;

    /** Optional; used for BANK_CARD */
    private String email;

    private String customerName;
}
