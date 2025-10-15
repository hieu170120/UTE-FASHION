
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ShopDTO;
import com.example.demo.service.BrandService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ShopService;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private ShopService shopService;

	private void addCommonAttributes(Model model) {
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
		model.addAttribute("shops", shopService.getAllShops());
	}

	@GetMapping
	public String listAllProducts(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "9") int size,
			@RequestParam(name = "keyword", required = false) String keyword) {

		Page<ProductDTO> productPage;
		if (keyword != null && !keyword.isEmpty()) {
			productPage = productService.searchProducts(keyword, PageRequest.of(page, size));
			model.addAttribute("pageTitle", "Kết quả tìm kiếm cho '" + keyword + "'");
		} else {
			productPage = productService.getAllProducts(PageRequest.of(page, size));
			model.addAttribute("pageTitle", "Tất cả sản phẩm");
		}

		addCommonAttributes(model);
		model.addAttribute("productPage", productPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentCategory", null); // No specific category
		model.addAttribute("currentBrand", null); // No specific brand
		model.addAttribute("currentShop", null); // No specific shop

		return "product/list";
	}

	@GetMapping("/category/{categorySlug}")
	public String listProductsByCategory(Model model, @PathVariable String categorySlug,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "9") int size) {

		Page<ProductDTO> productPage = productService.getProductsByCategory(categorySlug, PageRequest.of(page, size));
		CategoryDTO category = categoryService.getCategoryBySlug(categorySlug);

		addCommonAttributes(model);
		model.addAttribute("productPage", productPage);
		model.addAttribute("pageTitle", "Sản phẩm thuộc danh mục '" + category.getCategoryName() + "'");
		model.addAttribute("currentCategory", categorySlug);
		model.addAttribute("currentBrand", null);
		model.addAttribute("currentShop", null);

		return "product/list";
	}

	@GetMapping("/brand/{brandSlug}")
	public String listProductsByBrand(Model model, @PathVariable String brandSlug,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "9") int size) {

		Page<ProductDTO> productPage = productService.getProductsByBrand(brandSlug, PageRequest.of(page, size));
		BrandDTO brand = brandService.getBrandBySlug(brandSlug);

		addCommonAttributes(model);
		model.addAttribute("productPage", productPage);
		model.addAttribute("pageTitle", "Sản phẩm thuộc thương hiệu '" + brand.getBrandName() + "'");
		model.addAttribute("currentBrand", brandSlug);
		model.addAttribute("currentCategory", null);
		model.addAttribute("currentShop", null);

		return "product/list";
	}

	@GetMapping("/shop/{shopId}")
	public String listProductsByShop(Model model, @PathVariable Integer shopId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "9") int size) {

		Page<ProductDTO> productPage = productService.getProductsByShop(shopId, PageRequest.of(page, size));
		ShopDTO shop = shopService.getShopById(shopId);

		addCommonAttributes(model);
		model.addAttribute("productPage", productPage);
		model.addAttribute("pageTitle", "Sản phẩm của shop '" + shop.getShopName() + "'");
		model.addAttribute("currentShop", shopId);
		model.addAttribute("currentCategory", null);
		model.addAttribute("currentBrand", null);

		return "product/list";
	}

	@GetMapping("/{slug}")
	public String viewProduct(@PathVariable String slug, Model model) {
		try {
			ProductDTO product = productService.getProductBySlug(slug);
			model.addAttribute("pageTitle", product.getProductName());
			model.addAttribute("product", product);
			return "product/detail";
		} catch (Exception e) {
			return "redirect:/products";
		}
	}
}
