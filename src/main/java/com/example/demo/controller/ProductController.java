package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductSearchCriteria;
import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReviewService;

@Controller
public class ProductController {

	@Autowired
	private ProductService productService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ReviewService reviewService;

	@GetMapping("/products")
	public String listAndFilterProducts(@ModelAttribute ProductSearchCriteria criteria, Model model,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {

		Page<ProductSummaryDTO> productPage = productService.searchAndFilterProducts(criteria,
				PageRequest.of(page, size));

		String pageTitle = "Tất cả sản phẩm";
		if (StringUtils.hasText(criteria.getCategorySlug())) {
			try {
				CategoryDTO category = categoryService.getCategoryBySlug(criteria.getCategorySlug());
				pageTitle = category.getCategoryName();
			} catch (Exception e) {
				// Log the exception, but don't break the page. Fallback to slug.
				pageTitle = "Danh mục: " + criteria.getCategorySlug();
			}
		}

		model.addAttribute("productPage", productPage);
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("criteria", criteria);

		model.addAttribute("currentCategory", criteria.getCategorySlug());
		model.addAttribute("currentBrand", criteria.getBrandSlug());
		model.addAttribute("keyword", criteria.getKeyword());
		model.addAttribute("sort", criteria.getSort());

		return "product/products";
	}

	@GetMapping("/products/{slug}")
	public String viewProduct(@PathVariable String slug, Model model) {
		try {
			ProductDTO product = productService.findProductDetailBySlug(slug);
			model.addAttribute("product", product);
			model.addAttribute("pageTitle", product.getProductName());

			Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(product.getId(), PageRequest.of(0, 5));
			model.addAttribute("reviewPage", reviewPage);

			return "product/product-detail";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/products?error=notfound";
		}
	}
}
