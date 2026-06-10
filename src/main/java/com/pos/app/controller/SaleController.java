package com.pos.app.controller;

import com.pos.app.dto.chapa.ChapaRefundVerifyResponse;
import com.pos.app.dto.request.RefundRequest;
import com.pos.app.dto.request.SaleRequest;
import com.pos.app.dto.response.ApiResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.model.entity.User;
import com.pos.app.service.ChapaService;
import com.pos.app.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales & Payments", description = "Orders, Chapa payments, verification, and refunds")
public class SaleController {

    private final SaleService saleService;
    private final ChapaService chapaService;

    @PostMapping
    @Operation(summary = "Create sale / order", description = """
            Direct order without cart.
            CASH completes immediately. Online methods return PENDING and require verification.
            TELEBIRR/CBE_BIRR need phoneNumber. BANK_CARD returns checkoutUrl.
            """)
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

    @GetMapping("/{saleId}")
    @Operation(summary = "Get sale by ID")
    public ApiResponse<SaleResponse> getById(@PathVariable Long saleId) {
        return ApiResponse.success(
                "Sale fetched",
                saleService.getSaleById(saleId)
        );
    }

    @PostMapping("/{saleId}/refund")
    @Operation(summary = "Refund sale", description = """
            Refund a completed payment. Full refund restores stock.
            Online payments are refunded via Chapa. Cash refunds are processed locally.
            Omit amount for full refund.
            """)
    public ApiResponse<SaleResponse> refund(
            @PathVariable Long saleId,
            @RequestBody(required = false) RefundRequest request,
            @AuthenticationPrincipal User user
    ) {
        RefundRequest refundRequest = request != null ? request : new RefundRequest();
        SaleResponse response = saleService.refundSale(saleId, refundRequest, user);
        String message = response.getPaymentStatus().name().equals("REFUNDED")
                ? "Full refund processed successfully"
                : "Partial refund of " + response.getRefundAmount() + " ETB processed";
        return ApiResponse.success(message, response);
    }

    @GetMapping("/refund/verify")
    @Operation(summary = "Verify Chapa refund status", description = "Check refund status using ref_id from refund response")
    public ApiResponse<ChapaRefundVerifyResponse> verifyRefund(@RequestParam("ref_id") String refId) {
        ChapaRefundVerifyResponse response = chapaService.verifyRefund(refId);
        return ApiResponse.success("Refund status fetched", response);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verify Chapa payment using tx_ref after customer pays")
    public ApiResponse<SaleResponse> verify(@RequestParam("tx_ref") String txRef) {
        SaleResponse response = saleService.verifyPayment(txRef);
        String message = response.getPaymentStatus().name().equals("COMPLETED")
                ? "Payment verified and sale completed"
                : "Payment verification result: " + response.getPaymentStatus();
        return ApiResponse.success(message, response);
    }

    @GetMapping("/chapa/callback")
    @Operation(summary = "Chapa payment callback", description = "Called by Chapa after payment. No auth required.")
    @SecurityRequirements
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
    @Operation(summary = "Chapa return URL", description = "Customer redirect after payment. No auth required.")
    @SecurityRequirements
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
