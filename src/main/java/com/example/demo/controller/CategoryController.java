package com.example.demo.controller;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/{slug}")
    public String viewCategoryProducts(@PathVariable String slug, Model model) {
        CategoryDTO category = categoryService.getCategoryBySlug(slug); // Giả định phương thức này tồn tại
        List<ProductDTO> products = productService.getProductsByCategorySlug(slug); // Giả định phương thức này tồn tại

        model.addAttribute("pageTitle", "Sản phẩm thuộc danh mục " + category.getCategoryName());
        model.addAttribute("category", category);
        model.addAttribute("products", products);

        return "product/list"; // Tái sử dụng view list sản phẩm
    }
}
