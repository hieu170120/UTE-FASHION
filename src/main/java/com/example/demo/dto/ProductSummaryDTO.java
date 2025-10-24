package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
