//package com.example.demo.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import com.example.demo.dto.CategoryDTO;
//import com.example.demo.dto.ProductDTO;
//import com.example.demo.service.CategoryService;
//import com.example.demo.service.ProductService;
//
//@Controller
//@RequestMapping("/categories")
//public class CategoryController {
//
//	@Autowired
//	private CategoryService categoryService;
//
//	@Autowired
//	private ProductService productService;
//
//	@GetMapping("/{slug}")
//	public String viewCategoryProducts(@PathVariable String slug, Model model,
//			@RequestParam(name = "page", defaultValue = "0") int page,
//			@RequestParam(name = "size", defaultValue = "12") int size) {
//		CategoryDTO category = categoryService.getCategoryBySlug(slug);
//		Page<ProductDTO> products = productService.getProductsByCategory(slug, PageRequest.of(page, size));
//
//		model.addAttribute("pageTitle", "Sản phẩm thuộc danh mục " + category.getCategoryName());
//		model.addAttribute("category", category);
//		model.addAttribute("products", products);
//
//		return "product/list"; // Tái sử dụng view list sản phẩm
//	}
//}
