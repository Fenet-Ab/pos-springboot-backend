package com.pos.app.service;

import com.pos.app.config.ChapaProperties;
import com.pos.app.dto.chapa.ChapaInitializeResponse;
import com.pos.app.dto.chapa.ChapaRefundResponse;
import com.pos.app.dto.chapa.ChapaRefundVerifyResponse;
import com.pos.app.dto.chapa.ChapaVerifyResponse;
import com.pos.app.model.entity.Sale;
import com.pos.app.model.entity.User;
import com.pos.app.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ChapaService {

    private static final String BASE_URL = "https://api.chapa.co/v1";

    private final RestClient restClient;
    private final ChapaProperties chapaProperties;

    public String initiatePayment(
            Sale sale,
            PaymentMethod paymentMethod,
            String phoneNumber,
            String email,
            String customerName
    ) {
        return switch (paymentMethod) {
            case TELEBIRR -> initiateDirectCharge(sale, "telebirr", phoneNumber);
            case CBE_BIRR -> initiateDirectCharge(sale, "cbebirr", phoneNumber);
            case BANK_CARD -> initializeCheckout(sale, email, customerName);
            default -> throw new IllegalArgumentException("Unsupported online payment method: " + paymentMethod);
        };
    }

    public String refundTransaction(String txRef, java.math.BigDecimal amount, String reason) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (amount != null) {
            body.add("amount", formatAmount(amount));
        }
        if (reason != null && !reason.isBlank()) {
            body.add("reason", reason.trim());
        }

        ChapaRefundResponse response = restClient.post()
                .uri(BASE_URL + "/refund/" + txRef)
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(ChapaRefundResponse.class);

        if (response == null || !"success".equalsIgnoreCase(response.getStatus())) {
            String message = response != null ? response.getMessage() : "No response from Chapa";
            throw new RuntimeException("Chapa refund failed: " + message);
        }

        if (response.getData() == null || response.getData().getRef_id() == null) {
            throw new RuntimeException("Chapa refund failed: missing refund reference");
        }

        return response.getData().getRef_id();
    }

    public ChapaRefundVerifyResponse verifyRefund(String refId) {
        return restClient.get()
                .uri(BASE_URL + "/refund/" + refId + "/verify")
                .header("Authorization", bearerToken())
                .retrieve()
                .body(ChapaRefundVerifyResponse.class);
    }

    public boolean verifyTransaction(String txRef) {
        ChapaVerifyResponse response = restClient.get()
                .uri(BASE_URL + "/transaction/verify/" + txRef)
                .header("Authorization", bearerToken())
                .retrieve()
                .body(ChapaVerifyResponse.class);

        return response != null
                && "success".equalsIgnoreCase(response.getStatus())
                && response.getData() != null
                && "success".equalsIgnoreCase(response.getData().getStatus());
    }

    private String initializeCheckout(Sale sale, String email, String customerName) {
        User cashier = sale.getCashier();
        String resolvedEmail = resolveEmail(email, cashier);
        String[] nameParts = resolveName(customerName, cashier);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("amount", formatAmount(sale.getTotalAmount()));
        body.add("currency", "ETB");
        body.add("email", resolvedEmail);
        body.add("first_name", nameParts[0]);
        body.add("last_name", nameParts[1]);
        body.add("tx_ref", sale.getTxRef());
        body.add("callback_url", chapaProperties.getCallbackUrl());
        body.add("return_url", chapaProperties.getReturnUrl());

        ChapaInitializeResponse response = restClient.post()
                .uri(BASE_URL + "/transaction/initialize")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(ChapaInitializeResponse.class);

        if (response == null || !"success".equalsIgnoreCase(response.getStatus())) {
            String message = response != null ? response.getMessage() : "No response from Chapa";
            throw new RuntimeException("Chapa checkout failed: " + message);
        }

        return response.getData().getCheckout_url();
    }

    private String initiateDirectCharge(Sale sale, String chapaType, String phoneNumber) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("amount", formatAmount(sale.getTotalAmount()));
        body.add("currency", "ETB");
        body.add("tx_ref", sale.getTxRef());
        body.add("mobile", normalizePhone(phoneNumber));

        ChapaInitializeResponse response = restClient.post()
                .uri(BASE_URL + "/charges?type=" + chapaType)
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(ChapaInitializeResponse.class);

        if (response == null || !"success".equalsIgnoreCase(response.getStatus())) {
            String message = response != null ? response.getMessage() : "No response from Chapa";
            throw new RuntimeException("Chapa " + chapaType + " charge failed: " + message);
        }

        return null;
    }

    private String bearerToken() {
        return "Bearer " + chapaProperties.getSecretKey();
    }

    private String formatAmount(java.math.BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String resolveEmail(String email, User cashier) {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (cashier.getEmail() != null && !cashier.getEmail().isBlank()) {
            return cashier.getEmail();
        }
        return "customer@pos.local";
    }

    private String[] resolveName(String customerName, User cashier) {
        String name = customerName;
        if (name == null || name.isBlank()) {
            name = cashier.getFullName();
        }
        String[] parts = name.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "Customer";
        return new String[]{firstName, lastName};
    }

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("251") && digits.length() == 12) {
            return "0" + digits.substring(3);
        }
        if (digits.startsWith("0") && digits.length() == 10) {
            return digits;
        }
        throw new RuntimeException("Invalid phone number. Use format 09xxxxxxxx or 2519xxxxxxxx");
    }
}
