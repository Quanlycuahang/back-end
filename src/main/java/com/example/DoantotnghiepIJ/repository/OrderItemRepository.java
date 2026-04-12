package com.example.DoantotnghiepIJ.repository;

import com.example.DoantotnghiepIJ.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}