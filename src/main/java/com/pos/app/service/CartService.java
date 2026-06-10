package com.pos.app.service;

import com.pos.app.dto.request.AddToCartRequest;
import com.pos.app.dto.request.CheckoutCartRequest;
import com.pos.app.dto.request.UpdateCartItemRequest;
import com.pos.app.dto.response.CartResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.model.entity.User;

public interface CartService {

    CartResponse addToCart(AddToCartRequest request, User user);

    CartResponse getCart(User user);

    CartResponse updateItem(Long productId, UpdateCartItemRequest request, User user);

    CartResponse removeItem(Long productId, User user);

    CartResponse clearCart(User user);

    SaleResponse checkout(CheckoutCartRequest request, User user);
}
