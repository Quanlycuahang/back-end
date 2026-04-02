package com.example.DoantotnghiepIJ.dto.Discount;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {

    private String code;
    private String name;
    private String description;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}