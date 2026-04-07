package com.example.DoantotnghiepIJ.service;


import com.example.DoantotnghiepIJ.dto.cart.CartResponse;
import com.example.DoantotnghiepIJ.dto.cart.CartItemResponse;
import com.example.DoantotnghiepIJ.entity.Cart.Cart;
import com.example.DoantotnghiepIJ.entity.Cart.CartItem;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.mapper.CartMapper;
import com.example.DoantotnghiepIJ.repository.CartItemRepository;
import com.example.DoantotnghiepIJ.repository.CartRepository;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.DoantotnghiepIJ.dto.Menu.ProductResponse;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;
    private final UserRepository UserRepository;

    private final Long userId = 1L; // tạm hardcode
    private Cart getOrCreateCart() {
        User user = UserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .build()
                ));
    }
    // ===== GET CART =====
    public CartResponse getCart() {
        User user = UserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));

        if (cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .items(List.of())
                    .subtotal(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .build();
        }

        //  1. lấy productIds
        List<UUID> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .toList();

        //  2. gọi product API
        List<ProductResponse> products = productClient.getProducts(productIds);

        //  3. convert sang map
        Map<UUID, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        //  4. merge
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

                            //  QUAN TRỌNG
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

    // ===== ADD =====
    public CartResponse addToCart(UUID productId, int quantity) {

        Cart cart = getOrCreateCart();

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
        return getCart();
    }

    // ===== UPDATE =====
    public CartResponse updateQuantity(UUID productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
        }
        if (quantity > 100) {
            throw new RuntimeException("Quantity too large");
        }

        cartRepository.save(cart);
        return getCart();
    }

    // ===== DELETE =====
    public CartResponse removeItem(UUID productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow();

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow();

        cart.removeItem(item);
        cartRepository.save(cart);

        return getCart();
    }
//delete all item in cart
    public void clearCart() {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));


        cart.getItems().clear();

        cartRepository.save(cart);
    }
}