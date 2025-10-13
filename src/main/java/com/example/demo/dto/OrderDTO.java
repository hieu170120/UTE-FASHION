package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer id;
    private String orderNumber;
    private Integer userId;
    private String recipientName;
    private String phoneNumber;
    private String email;
    private String shippingAddress;
    private String ward;
    private String district;
    private String city;
    private String postalCode;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String customerNotes;
    private String adminNotes;
    private LocalDateTime orderDate;
    private List<OrderItemDTO> orderItems;
}