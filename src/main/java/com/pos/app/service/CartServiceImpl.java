package com.pos.app.service;

import com.pos.app.dto.request.AddToCartRequest;
import com.pos.app.dto.request.CheckoutCartRequest;
import com.pos.app.dto.request.SaleItemRequest;
import com.pos.app.dto.request.SaleRequest;
import com.pos.app.dto.request.UpdateCartItemRequest;
import com.pos.app.dto.response.CartItemResponse;
import com.pos.app.dto.response.CartResponse;
import com.pos.app.dto.response.SaleResponse;
import com.pos.app.exception.ResourceNotFoundException;
import com.pos.app.model.entity.Cart;
import com.pos.app.model.entity.CartItem;
import com.pos.app.model.entity.Product;
import com.pos.app.model.entity.User;
import com.pos.app.repository.CartItemRepository;
import com.pos.app.repository.CartRepository;
import com.pos.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SaleService saleService;

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request, User user) {
        if (request.getProductId() == null) {
            throw new RuntimeException("Product id is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()
                ));

        Cart cart = getOrCreateCart(user);
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        int newQuantity = request.getQuantity();
        if (existingItem != null) {
            newQuantity = existingItem.getQuantity() + request.getQuantity();
        }

        if (product.getQuantity() < newQuantity) {
            throw new RuntimeException(
                    "Insufficient stock for " + product.getName()
                            + ". Available: " + product.getQuantity()
            );
        }

        if (existingItem != null) {
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(cartItem);
        }

        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> Cart.builder().user(user).build());
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long productId, UpdateCartItemRequest request, User user) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Cart cart = getCartOrThrow(user);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        if (item.getProduct().getQuantity() < request.getQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock for " + item.getProduct().getName()
                            + ". Available: " + item.getProduct().getQuantity()
            );
        }

        item.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long productId, User user) {
        Cart cart = getCartOrThrow(user);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        cart.getItems().remove(item);
        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return emptyCartResponse();
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public SaleResponse checkout(CheckoutCartRequest request, User user) {
        Cart cart = getCartOrThrow(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Add products before checkout.");
        }

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setPaymentMethod(request.getPaymentMethod());
        saleRequest.setPhoneNumber(request.getPhoneNumber());
        saleRequest.setEmail(request.getEmail());
        saleRequest.setCustomerName(request.getCustomerName());
        saleRequest.setItems(cart.getItems().stream()
                .map(item -> {
                    SaleItemRequest saleItem = new SaleItemRequest();
                    saleItem.setProductId(item.getProduct().getId());
                    saleItem.setQuantity(item.getQuantity());
                    return saleItem;
                })
                .toList());

        SaleResponse saleResponse = saleService.createSale(saleRequest, user);

        cart.getItems().clear();
        cartRepository.save(cart);

        return saleResponse;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    private Cart getCartOrThrow(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart is empty. Add products first."));
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getProduct().getPrice())
                            .subtotal(subtotal)
                            .availableStock(item.getProduct().getQuantity())
                            .build();
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .itemCount(items.size())
                .totalAmount(total)
                .items(items)
                .build();
    }

    private CartResponse emptyCartResponse() {
        return CartResponse.builder()
                .itemCount(0)
                .totalAmount(BigDecimal.ZERO)
                .items(List.of())
                .build();
    }
}
