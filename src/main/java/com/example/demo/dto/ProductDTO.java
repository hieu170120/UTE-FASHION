package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Set;

import lombok.Data;

@Data
public class ProductDTO {
	private Integer id;
	private String productName;
	private String slug;
	private String sku;
	private Integer categoryId;
	private Integer brandId;
	private Integer shopId;
	private String description;
	private String shortDescription;
	private BigDecimal price;
	private BigDecimal salePrice;
	private BigDecimal costPrice;
	private Integer stockQuantity;
	private Integer lowStockThreshold;
	private BigDecimal weight;
	private String dimensions;
	private String material;
	private boolean isFeatured;
	private boolean isNewArrival;
	private boolean isBestSeller;
	private boolean isActive;
	private Integer viewCount;
	private Integer soldCount;
	private BigDecimal averageRating;
	private Integer reviewCount;
	private Set<ProductImageDTO> images;
	private Set<ProductVariantDTO> variants;
}
