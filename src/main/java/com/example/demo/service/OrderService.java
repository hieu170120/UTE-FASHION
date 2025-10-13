package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO createOrderFromCart(Integer userId, String sessionId, OrderDTO orderDTO);
    OrderDTO getOrderById(Integer orderId);
    List<OrderDTO> getUserOrders(Integer userId);
    Page<OrderDTO> getAllOrders(Pageable pageable);
    OrderDTO updateOrderStatus(Integer orderId, String newStatus, String notes, Integer changedBy);
    void cancelOrder(Integer orderId, Integer userId);
}