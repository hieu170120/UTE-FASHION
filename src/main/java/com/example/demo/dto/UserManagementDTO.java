package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho quản lý User trong Admin
 */
@Data
public class UserManagementDTO {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thống kê đơn hàng
    private Long totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderDate;
}

