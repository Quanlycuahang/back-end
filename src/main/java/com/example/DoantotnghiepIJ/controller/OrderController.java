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

    // Tạo đơn
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    // Xem danh sách
    @GetMapping
    public List<Order> getAll() {
        return orderService.getAllOrders();
    }

    // Xem chi tiết
    @GetMapping("/{id}")
    public Order getDetail(@PathVariable String id) {
        return orderService.getOrderById(id);
    }

    // Cập nhật đơn (địa chỉ, ghi chú, ...)
    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable String id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    // Cập nhật trạng thái đơn
    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable String id,
                              @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    // Cập nhật trạng thái thanh toán
    @PatchMapping("/{id}/payment")
    public Order updatePayment(@PathVariable String id,
                               @RequestParam PaymentStatus paymentStatus) {
        return orderService.updatePaymentStatus(id, paymentStatus);
    }

    // Xóa đơn (soft delete)
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
    }
}