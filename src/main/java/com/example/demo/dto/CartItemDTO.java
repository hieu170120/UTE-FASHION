package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer cartItemId;
    private Integer cartId;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
