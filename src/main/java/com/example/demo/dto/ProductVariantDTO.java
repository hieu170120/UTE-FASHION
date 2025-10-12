package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantDTO {
    private Integer id;
    private String sku;
    private BigDecimal priceAdjustment;
    private Integer stockQuantity;
    private boolean isActive;
    private SizeDTO size;
    private ColorDTO color;
}
