package com.example.DoantotnghiepIJ.controller.client;



import com.example.DoantotnghiepIJ.dto.cart.AddToCartRequest;
import com.example.DoantotnghiepIJ.dto.cart.CartResponse;
import com.example.DoantotnghiepIJ.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponse addToCart(@RequestBody AddToCartRequest request) {


        //BigDecimal fakePrice = new BigDecimal("100000");

        return cartService.addToCart(
                request.getProductId(),
                request.getQuantity()
        );
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        return cartService.updateQuantity(productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable UUID productId) {
        return cartService.removeItem(productId);
    }
    @DeleteMapping
    public String clearCart() {
        cartService.clearCart();
        return "Cart cleared successfully";
    }
}