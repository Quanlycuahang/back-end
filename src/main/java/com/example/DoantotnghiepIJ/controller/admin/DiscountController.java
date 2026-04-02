package com.example.DoantotnghiepIJ.controller.admin;



import com.example.DoantotnghiepIJ.dto.Discount.DiscountRequest;
import com.example.DoantotnghiepIJ.entity.Discount;
import com.example.DoantotnghiepIJ.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public Page<Discount> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return discountService.getAllFull(page, size, sortBy, sortDir);
    }
    @PostMapping
    public Discount create(@RequestBody DiscountRequest request) {
        return discountService.create(request);
    }

    @PutMapping("/{id}")
    public Discount update(@PathVariable Long id,
                           @RequestBody DiscountRequest request) {
        return discountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        discountService.delete(id);
    }

    @GetMapping("/{code}")
    public Discount getByCode(@PathVariable String code) {
        return discountService.getByCode(code);
    }

    // 🔥 TEST APPLY
    @GetMapping("/apply")
    public BigDecimal apply(@RequestParam String code,
                            @RequestParam BigDecimal total) {
        return discountService.applyDiscount(code, total);
    }
}