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

    // Lấy chi tiết
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // Tạo đơn (bạn đã có logic trước đó)
    public Order createOrder(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Lấy danh sách
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Cập nhật thông tin đơn
    public Order updateOrder(String id, Order newOrder) {
        Order order = getOrderById(id);

        order.setReceiverName(newOrder.getReceiverName());
        order.setReceiverPhone(newOrder.getReceiverPhone());
        order.setShippingAddress(newOrder.getShippingAddress());
        order.setNote(newOrder.getNote());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Cập nhật trạng thái đơn
    public Order updateStatus(String id, OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Cập nhật thanh toán
    public Order updatePaymentStatus(String id, PaymentStatus paymentStatus) {
        Order order = getOrderById(id);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Xóa mềm
    public void deleteOrder(String id) {
        Order order = getOrderById(id);
        order.setDeleted(true);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}