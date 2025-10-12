package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {
    private Integer id;
    private String categoryName;
    private String slug;
    private String description;
    private Integer parentId;
    private List<CategoryDTO> subCategories;
    private String imageUrl;
    private boolean isActive;
}
