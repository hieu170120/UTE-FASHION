package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductSummaryDTO {

    private Integer id;
    private String productName;
    private String slug;
    private BigDecimal price;
    private BigDecimal salePrice;
    private List<ProductImageDTO> images;

    public ProductSummaryDTO(Integer id, String productName, String slug, BigDecimal price, BigDecimal salePrice) {
        this.id = id;
        this.productName = productName;
        this.slug = slug;
        this.price = price;
        this.salePrice = salePrice;
    }
}
