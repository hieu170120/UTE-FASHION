package com.example.demo.dto;

import lombok.Data;

@Data
public class ProductSearchCriteria {
    private String keyword;
    private String categorySlug;
    private String brandSlug;
    private String sort = "newest"; // Default sort order
}
