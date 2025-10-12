package com.example.demo.dto;

import lombok.Data;

@Data
public class BrandDTO {
    private Integer id;
    private String brandName;
    private String slug;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private boolean isActive;
}
