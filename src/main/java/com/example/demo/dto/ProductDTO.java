package com.example.demo.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductDTO {
	private Integer id;
	private String productName;
	private String slug;
	private String sku;
	private Integer categoryId;
	private Integer brandId;
    private String brandName;
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

	private Integer viewCount;
	private Integer soldCount;
	private BigDecimal averageRating;
	private Integer reviewCount;
    // Renamed fields to avoid Lombok/JavaBean convention conflicts
    private boolean featured;
    private boolean newArrival;
    private boolean bestSeller;
    private boolean active;

}
