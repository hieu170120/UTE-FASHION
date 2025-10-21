package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO để hiển thị lịch sử đơn hàng cho shipper
 * Bao gồm cả đơn thường và đơn đã hủy
 */
@Getter
@Setter
public class ShipperOrderHistoryItemDTO {
    private Integer orderId;
    private String orderNumber;
    private String recipientName;
    private String phoneNumber;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private String orderStatus; // "Delivered", "Shipped", "ShipperCancelled", etc.
    private LocalDateTime displayDate; // Ngày để sort (orderDate hoặc cancelledAt)
    private String cancelReason; // Lý do hủy (nếu là ShipperCancelled)
    private boolean isShipperCancelled; // Flag để biết đây là đơn shipper hủy
}
