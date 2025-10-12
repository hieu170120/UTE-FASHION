package com.example.demo.dto;

import lombok.Data;

@Data
public class ProductImageDTO {
    private Integer id;
    private String imageUrl;
    private String altText;
    private Integer displayOrder;
    private boolean isPrimary;
}
