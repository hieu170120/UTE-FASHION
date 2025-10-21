package com.example.demo.service;

import com.example.demo.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> findAllActive();
    CategoryDTO getCategoryById(Integer id);
    CategoryDTO getCategoryBySlug(String slug);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO);
    void deleteCategory(Integer id);
}
