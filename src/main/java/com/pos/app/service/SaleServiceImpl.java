package com.pos.app.service;

import com.pos.app.dto.request.SaleItemRequest;
import com.pos.app.dto.request.SaleRequest;
import com.pos.app.dto.response.SaleItemResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.exception.ResourceNotFoundException;
import com.pos.app.model.entity.Product;
import com.pos.app.model.entity.Sale;
import com.pos.app.model.entity.SaleItem;
import com.pos.app.model.entity.User;
import com.pos.app.model.enums.PaymentMethod;
import com.pos.app.model.enums.PaymentStatus;
import com.pos.app.repository.ProductRepository;
import com.pos.app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ChapaService chapaService;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request, User cashier) {
        validateRequest(request);

        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()
                    ));

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than zero for product: " + product.getName());
            }

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getName()
                                + ". Available: " + product.getQuantity()
                );
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            saleItems.add(SaleItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build());
        }

        boolean isCash = request.getPaymentMethod() == PaymentMethod.CASH;
        String txRef = "POS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        Sale sale = Sale.builder()
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(isCash ? PaymentStatus.COMPLETED : PaymentStatus.PENDING)
                .txRef(txRef)
                .cashier(cashier)
                .items(saleItems)
                .build();

        for (SaleItem item : saleItems) {
            item.setSale(sale);
        }

        if (isCash) {
            deductStock(saleItems);
        }

        sale = saleRepository.save(sale);

        if (!isCash) {
            String checkoutUrl = chapaService.initiatePayment(
                    sale,
                    request.getPaymentMethod(),
                    request.getPhoneNumber(),
                    request.getEmail(),
                    request.getCustomerName()
            );
            sale.setCheckoutUrl(checkoutUrl);
            sale = saleRepository.save(sale);
        }

        return mapToResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse verifyPayment(String txRef) {
        Sale sale = saleRepository.findByTxRef(txRef)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found for reference: " + txRef));

        if (sale.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return mapToResponse(sale);
        }

        if (sale.getPaymentMethod() == PaymentMethod.CASH) {
            return mapToResponse(sale);
        }

        boolean paid = chapaService.verifyTransaction(txRef);

        if (paid) {
            sale.setPaymentStatus(PaymentStatus.COMPLETED);
            deductStock(sale.getItems());
        } else {
            sale.setPaymentStatus(PaymentStatus.FAILED);
        }

        sale = saleRepository.save(sale);
        return mapToResponse(sale);
    }

    private void validateRequest(SaleRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Sale must contain at least one item");
        }

        if (request.getPaymentMethod() == null) {
            throw new RuntimeException("Payment method is required");
        }

        if (request.getPaymentMethod() == PaymentMethod.TELEBIRR
                || request.getPaymentMethod() == PaymentMethod.CBE_BIRR) {
            if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
                throw new RuntimeException("Phone number is required for " + request.getPaymentMethod());
            }
        }
    }

    private void deductStock(List<SaleItem> items) {
        for (SaleItem item : items) {
            Product product = item.getProduct();
            int remaining = product.getQuantity() - item.getQuantity();
            if (remaining < 0) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }
            product.setQuantity(remaining);
            productRepository.save(product);
        }
    }

    private SaleResponse mapToResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(item -> SaleItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return SaleResponse.builder()
                .saleId(sale.getId())
                .cashier(sale.getCashier().getFullName())
                .paymentMethod(sale.getPaymentMethod())
                .paymentStatus(sale.getPaymentStatus())
                .totalAmount(sale.getTotalAmount())
                .txRef(sale.getTxRef())
                .checkoutUrl(sale.getCheckoutUrl())
                .createdAt(sale.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
