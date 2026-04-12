package com.example.DoantotnghiepIJ.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String menuItemId;
    private String menuItemName;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;

    private String note;

    private boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}