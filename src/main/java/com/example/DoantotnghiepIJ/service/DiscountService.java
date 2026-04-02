package com.example.DoantotnghiepIJ.service;



import com.example.DoantotnghiepIJ.dto.Discount.DiscountRequest;
import com.example.DoantotnghiepIJ.entity.Discount;
import com.example.DoantotnghiepIJ.mapper.DiscountMapper;
import com.example.DoantotnghiepIJ.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    public Discount create(DiscountRequest request) {
        Discount discount = DiscountMapper.toEntity(request);
        return discountRepository.save(discount);
    }

    public Discount update(Long id, DiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        DiscountMapper.update(discount, request);
        return discountRepository.save(discount);
    }

    public void delete(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        discount.setIsDeleted(true);
        discountRepository.save(discount);
    }

    public Discount getByCode(String code) {
        return discountRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
    }

    // 🔥 APPLY DISCOUNT
    public BigDecimal applyDiscount(String code, BigDecimal orderTotal) {
        Discount discount = getByCode(code);

        if (!discount.getStatus()) throw new RuntimeException("Discount inactive");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
            throw new RuntimeException("Discount expired");
        }

        if (discount.getMinOrderValue() != null &&
                orderTotal.compareTo(discount.getMinOrderValue()) < 0) {
            throw new RuntimeException("Not enough order value");
        }

        BigDecimal discountAmount;

        if (discount.getDiscountType() == 0) {
            // %
            discountAmount = orderTotal
                    .multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));

            if (discount.getMaxDiscount() != null &&
                    discountAmount.compareTo(discount.getMaxDiscount()) > 0) {
                discountAmount = discount.getMaxDiscount();
            }
        } else {
            // tiền
            discountAmount = discount.getDiscountValue();
        }

        return orderTotal.subtract(discountAmount);
    }
}