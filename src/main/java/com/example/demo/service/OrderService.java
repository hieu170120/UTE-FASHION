package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderDTO createOrderFromCart(Integer userId, String sessionId, OrderDTO orderDTO);
    OrderDTO getOrderById(Integer orderId);
    List<OrderDTO> getUserOrders(Integer userId);
    Page<OrderDTO> getUserOrdersPageable(Integer userId, Pageable pageable);
    Page<OrderDTO> getAllOrders(Pageable pageable);
    OrderDTO updateOrderStatus(Integer orderId, String newStatus, String notes, Integer changedBy);
    void cancelOrder(Integer orderId, Integer userId);
    void assignShipperAndConfirm(Integer orderId, Integer shipperId, Integer adminId);
    List<OrderDTO> getOrdersByShipper(Integer shipperId);
    Map<String, Object> getShipperOrderStats(Integer shipperId);
    void shipperConfirmOrder(Integer orderId, Integer shipperId);
    void shipperCancelOrder(Integer orderId, Integer shipperId);
    void requestReturn(Integer orderId, Integer userId, String notes);
    void approveReturn(Integer orderId, String notes, Integer adminId);
    void rejectReturn(Integer orderId, String notes, Integer adminId);
    void completeDelivery(Integer orderId);
    Page<OrderDTO> findOrdersByFilters(String status, String fromDate, String toDate, Pageable pageable);
    Page<OrderDTO> getOrdersByShopId(Integer shopId, Pageable pageable);
    java.util.Optional<OrderDTO> getOrderDetails(Integer orderId);
    Page<OrderDTO> getOrdersByShopIdWithFilters(Integer shopId, String status, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}