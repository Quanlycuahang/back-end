package com.example.DoantotnghiepIJ.controller;

import com.example.DoantotnghiepIJ.Enum.OrderStatus;
import com.example.DoantotnghiepIJ.Enum.PaymentStatus;
import com.example.DoantotnghiepIJ.entity.Order;
import com.example.DoantotnghiepIJ.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================
    // TẠO ĐƠN
    // =========================
    @PostMapping
    public Order createOrder(@RequestBody Order order) {

        // ❗ check userId
        if (order.getUserId() == null || order.getUserId().isEmpty()) {
            throw new RuntimeException("userId is required");
        }

        return orderService.createOrder(order);
    }

    // =========================
    // XEM DANH SÁCH
    // =========================
    @GetMapping
    public List<Order> getAll() {
        return orderService.getAllOrders();
    }

    // =========================
    // XEM CHI TIẾT
    // =========================
    @GetMapping("/{id}")
    public Order getDetail(@PathVariable String id) {
        return orderService.getOrderById(id);
    }

    // =========================
    // 🔥 LẤY ĐƠN THEO USER
    // =========================
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable String userId) {
        return orderService.getOrdersByUser(userId);
    }

    // =========================
    // CẬP NHẬT ĐƠN
    // =========================
    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable String id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    // =========================
    // CẬP NHẬT TRẠNG THÁI
    // =========================
    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable String id,
                              @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    // =========================
    // CẬP NHẬT THANH TOÁN
    // =========================
    @PatchMapping("/{id}/payment")
    public Order updatePayment(@PathVariable String id,
                               @RequestParam PaymentStatus paymentStatus) {
        return orderService.updatePaymentStatus(id, paymentStatus);
    }

    // =========================
    // XÓA MỀM
    // =========================
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
    }
}