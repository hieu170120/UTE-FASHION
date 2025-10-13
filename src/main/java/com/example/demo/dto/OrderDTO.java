package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer id;
    private String orderNumber;
    private Integer userId;
    @NotBlank(message = "Họ tên không được để trống")
    private String recipientName;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;
    private String email;
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
    private String ward;
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;
    @NotBlank(message = "Thành phố không được để trống")
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