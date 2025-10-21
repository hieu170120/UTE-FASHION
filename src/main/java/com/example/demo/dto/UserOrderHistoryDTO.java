package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho lịch sử đơn hàng của User trong Admin
 */
@Data
public class UserOrderHistoryDTO {
    private Integer orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String orderStatus;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String shippingAddress;
    private String recipientName;
    private String phoneNumber;
}
