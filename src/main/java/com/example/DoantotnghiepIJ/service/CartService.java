package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.dto.Menu.ProductResponse;
import com.example.DoantotnghiepIJ.dto.cart.CartResponse;
import com.example.DoantotnghiepIJ.dto.cart.CartItemResponse;
import com.example.DoantotnghiepIJ.entity.Cart.Cart;
import com.example.DoantotnghiepIJ.entity.Cart.CartItem;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.repository.CartItemRepository;
import com.example.DoantotnghiepIJ.repository.CartRepository;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductClient productClient;

    // =========================
    // 🔥 CORE: lấy cart theo login hoặc guest
    // =========================
    private Cart getCurrentCart(String sessionId) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        // 👉 CASE 1: đã login
        if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())) {

            String email = auth.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> cartRepository.save(
                            Cart.builder()
                                    .user(user)
                                    .build()
                    ));
        }

        // 👉 CASE 2: guest
        if (sessionId == null || sessionId.isBlank()) {
            throw new RuntimeException("SessionId is required for guest");
        }

        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .sessionId(sessionId)
                                .build()
                ));
    }

    // =========================
    // 🛒 GET CART
    // =========================
    public CartResponse getCart(String sessionId) {

        Cart cart = getCurrentCart(sessionId);

        if (cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .items(List.of())
                    .subtotal(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .build();
        }

        List<UUID> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductResponse> products = productClient.getProducts(productIds);

        Map<UUID, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    ProductResponse p = productMap.get(item.getProductId());

                    if (p == null) {
                        throw new RuntimeException("Product not found");
                    }

                    return CartItemResponse.builder()
                            .productId(item.getProductId())
                            .name(p.getName())
                            .thumbnail(p.getThumbnail())
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .total(item.getTotal())
                            .build();
                })
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .subtotal(subtotal)
                .total(subtotal)
                .build();
    }

    // =========================
    // ➕ ADD TO CART
    // =========================
    public CartResponse addToCart(UUID productId, int quantity, String sessionId) {

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be > 0");
        }

        Cart cart = getCurrentCart(sessionId);

        List<ProductResponse> products = productClient.getProducts(List.of(productId));

        if (products.isEmpty()) {
            throw new RuntimeException("Product not found: " + productId);
        }

        ProductResponse product = products.get(0);

        Double finalPrice = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getPrice();

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = CartItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .price(BigDecimal.valueOf(finalPrice))
                    .build();

            cart.addItem(item);
        }

        cartRepository.save(cart);
        return getCart(sessionId);
    }

    // =========================
    // 🔄 UPDATE QUANTITY
    // =========================
    public CartResponse updateQuantity(UUID productId, int quantity, String sessionId) {

        Cart cart = getCurrentCart(sessionId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (quantity > 100) {
            throw new RuntimeException("Quantity too large");
        }

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
        }

        cartRepository.save(cart);
        return getCart(sessionId);
    }

    // =========================
    // ❌ REMOVE ITEM
    // =========================
    public CartResponse removeItem(UUID productId, String sessionId) {

        Cart cart = getCurrentCart(sessionId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        cart.removeItem(item);
        cartRepository.save(cart);

        return getCart(sessionId);
    }

    // =========================
    // 🧹 CLEAR CART
    // =========================
    public void clearCart(String sessionId) {

        Cart cart = getCurrentCart(sessionId);

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}