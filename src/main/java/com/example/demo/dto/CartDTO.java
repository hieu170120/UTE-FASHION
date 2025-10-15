package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {
    private Integer cartId;
    private Integer userId;
    private String sessionId;
    private List<CartItemDTO> cartItems;
    private BigDecimal totalAmount;
}
