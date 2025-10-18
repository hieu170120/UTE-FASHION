package com.example.demo.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.service.BrandService;
import com.example.demo.service.CategoryService;

@RestController
@RequestMapping("/api/filters")
public class FilterApiController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private BrandService brandService;

	@GetMapping("/categories")
	public ResponseEntity<List<CategoryDTO>> getCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	@GetMapping("/brands")
	public ResponseEntity<List<BrandDTO>> getBrands() {
		return ResponseEntity.ok(brandService.getAllBrands());
	}
}
