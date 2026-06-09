package com.pos.app.dto.request;

import com.pos.app.model.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class SaleRequest {

    private List<SaleItemRequest> items;

    private PaymentMethod paymentMethod;

    /** Required for TELEBIRR and CBE_BIRR (e.g. 0912345678) */
    private String phoneNumber;

    /** Optional; used for BANK_CARD Chapa checkout */
    private String email;

    /** Optional customer name for Chapa checkout */
    private String customerName;
}