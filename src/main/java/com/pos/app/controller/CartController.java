package com.pos.app.controller;

import com.pos.app.dto.request.AddToCartRequest;
import com.pos.app.dto.request.CheckoutCartRequest;
import com.pos.app.dto.request.UpdateCartItemRequest;
import com.pos.app.dto.response.ApiResponse;
import com.pos.app.dto.response.CartResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.model.entity.User;
import com.pos.app.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart — add items then checkout to create an order")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @Operation(summary = "Add product to cart")
    public ApiResponse<CartResponse> addToCart(
            @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.success(
                "Product added to cart",
                cartService.addToCart(request, user)
        );
    }

    @GetMapping
    @Operation(summary = "Get current cart")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.success(
                "Cart fetched",
                cartService.getCart(user)
        );
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item quantity")
    public ApiResponse<CartResponse> updateItem(
            @PathVariable Long productId,
            @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.success(
                "Cart item updated",
                cartService.updateItem(productId, request, user)
        );
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ApiResponse<CartResponse> removeItem(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.success(
                "Item removed from cart",
                cartService.removeItem(productId, user)
        );
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ApiResponse<CartResponse> clearCart(@AuthenticationPrincipal User user) {
        return ApiResponse.success(
                "Cart cleared",
                cartService.clearCart(user)
        );
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = """
            Creates an order from cart items and clears the cart.
            Payment methods: CASH, TELEBIRR, CBE_BIRR, BANK_CARD.
            Phone number required for TELEBIRR and CBE_BIRR.
            """)
    public ApiResponse<SaleResponse> checkout(
            @RequestBody CheckoutCartRequest request,
            @AuthenticationPrincipal User user
    ) {
        SaleResponse response = cartService.checkout(request, user);
        String message = response.getPaymentStatus().name().equals("COMPLETED")
                ? "Order completed from cart"
                : "Order created from cart. Complete payment, then verify with tx_ref.";
        return ApiResponse.success(message, response);
    }
}
