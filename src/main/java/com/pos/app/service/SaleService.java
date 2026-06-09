package com.pos.app.service;
import com.pos.app.dto.request.SaleRequest;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.model.entity.User;

public interface SaleService {

    SaleResponse createSale(
            SaleRequest request,
            User cashier
    );

    SaleResponse verifyPayment(String txRef);
}