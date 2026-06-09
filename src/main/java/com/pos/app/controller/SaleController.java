package com.pos.app.controller;

import com.pos.app.dto.request.SaleRequest;
import com.pos.app.dto.response.ApiResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.model.entity.User;
import com.pos.app.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ApiResponse<SaleResponse> create(
            @RequestBody SaleRequest request,
            @AuthenticationPrincipal User user
    ) {
        SaleResponse response = saleService.createSale(request, user);
        String message = switch (request.getPaymentMethod()) {
            case CASH -> "Cash sale completed successfully";
            case TELEBIRR, CBE_BIRR -> "Payment initiated. Check the customer's phone for USSD prompt, then verify payment.";
            case BANK_CARD -> response.getCheckoutUrl() != null
                    ? "Open checkout URL to complete card payment, then verify."
                    : "Card payment initiated. Verify payment after completion.";
        };
        return ApiResponse.success(message, response);
    }

    @GetMapping("/verify")
    public ApiResponse<SaleResponse> verify(@RequestParam("tx_ref") String txRef) {
        SaleResponse response = saleService.verifyPayment(txRef);
        String message = response.getPaymentStatus().name().equals("COMPLETED")
                ? "Payment verified and sale completed"
                : "Payment verification result: " + response.getPaymentStatus();
        return ApiResponse.success(message, response);
    }

    @GetMapping("/chapa/callback")
    public ApiResponse<SaleResponse> chapaCallback(@RequestParam(value = "trx_ref", required = false) String trxRef,
                                                   @RequestParam(value = "tx_ref", required = false) String txRef) {
        String reference = trxRef != null ? trxRef : txRef;
        if (reference == null || reference.isBlank()) {
            throw new RuntimeException("Missing transaction reference in callback");
        }
        SaleResponse response = saleService.verifyPayment(reference);
        return ApiResponse.success("Callback processed", response);
    }

    @GetMapping("/chapa/return")
    public ApiResponse<SaleResponse> chapaReturn(@RequestParam(value = "trx_ref", required = false) String trxRef,
                                                 @RequestParam(value = "tx_ref", required = false) String txRef) {
        String reference = trxRef != null ? trxRef : txRef;
        if (reference == null || reference.isBlank()) {
            throw new RuntimeException("Missing transaction reference in return URL");
        }
        SaleResponse response = saleService.verifyPayment(reference);
        return ApiResponse.success("Return URL processed", response);
    }
}
