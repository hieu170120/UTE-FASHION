package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A Data Transfer Object for summarizing a Product.
 * This DTO is designed for lists and grids where only essential information is needed.
 * It purposefully does NOT contain collections like images or variants to allow for
 * efficient queries and client-side lazy loading.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private Integer productId;
    private String productName;
    private String slug;
    private BigDecimal price;
    private BigDecimal salePrice;
}
