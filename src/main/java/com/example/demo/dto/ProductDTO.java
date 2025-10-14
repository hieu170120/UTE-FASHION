package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductDTO {
    private Integer id;
    private String productName;
    private String slug;
    private String shortDescription;
    private String longDescription;
    private BigDecimal basePrice;
    private String sku;
    private Integer stockQuantity;
    private String taxClass;
    private boolean isActive;
    private CategoryDTO category;
    private BrandDTO brand;
    private Set<ProductImageDTO> images;
    private Set<ProductVariantDTO> variants;
}
