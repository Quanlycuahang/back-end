package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.Enum.OrderStatus;
import com.example.DoantotnghiepIJ.Enum.PaymentStatus;
import com.example.DoantotnghiepIJ.entity.Order;
import com.example.DoantotnghiepIJ.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // =========================
    // LẤY CHI TIẾT
    // =========================
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // =========================
    // TẠO ĐƠN HÀNG
    // =========================
    public Order createOrder(Order order) {

        // ❗ userId phải được set từ Controller
        if (order.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }

        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Gán quan hệ Order - OrderItem
        order.getItems().forEach(item -> item.setOrder(order));

        return orderRepository.save(order);
    }

    // =========================
    // LẤY DANH SÁCH TẤT CẢ ĐƠN
    // =========================
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // =========================
    // LẤY ĐƠN THEO USER
    // =========================
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    // =========================
    // CẬP NHẬT ĐƠN
    // =========================
    public Order updateOrder(String id, Order newOrder) {
        Order order = getOrderById(id);

        order.setReceiverName(newOrder.getReceiverName());
        order.setReceiverPhone(newOrder.getReceiverPhone());
        order.setShippingAddress(newOrder.getShippingAddress());
        order.setNote(newOrder.getNote());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // =========================
    // CẬP NHẬT TRẠNG THÁI
    // =========================
    public Order updateStatus(String id, OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // =========================
    // CẬP NHẬT THANH TOÁN
    // =========================
    public Order updatePaymentStatus(String id, PaymentStatus paymentStatus) {
        Order order = getOrderById(id);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // =========================
    // XÓA MỀM
    // =========================
    public void deleteOrder(String id) {
        Order order = getOrderById(id);
        order.setDeleted(true);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}