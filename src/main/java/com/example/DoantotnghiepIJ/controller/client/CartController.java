package com.example.DoantotnghiepIJ.controller.client;

import com.example.DoantotnghiepIJ.dto.cart.AddToCartRequest;
import com.example.DoantotnghiepIJ.dto.cart.CartResponse;
import com.example.DoantotnghiepIJ.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // =========================
    //  GET CART
    // =========================
    @GetMapping
    public CartResponse getCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        return cartService.getCart(sessionId);
    }

    // =========================
    //  ADD TO CART
    // =========================
    @PostMapping("/items")
    public CartResponse addToCart(
            @RequestBody AddToCartRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        return cartService.addToCart(
                request.getProductId(),
                request.getQuantity(),
                sessionId
        );
    }

    // =========================
    //  UPDATE QUANTITY
    // =========================
    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        return cartService.updateQuantity(productId, quantity, sessionId);
    }

    // =========================
    //  REMOVE ITEM
    // =========================
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        return cartService.removeItem(productId, sessionId);
    }

    // =========================
    //  CLEAR CART
    // =========================
    @DeleteMapping
    public String clearCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        cartService.clearCart(sessionId);
        return "Cart cleared successfully";
    }
}