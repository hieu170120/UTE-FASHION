//package com.example.demo.controller.admin;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.example.demo.dto.ProductDTO;
//import com.example.demo.service.BrandService;
//import com.example.demo.service.CategoryService;
//import com.example.demo.service.ProductService;
//import com.example.demo.service.ShopService;
//
//import jakarta.validation.Valid;
//
//@Controller
//@RequestMapping("/admin/products")
//public class AdminProductController {
//
//	@Autowired
//	private ProductService productService;
//
//	@Autowired
//	private CategoryService categoryService;
//
//	@Autowired
//	private BrandService brandService;
//
//	@Autowired
//	private ShopService shopService;
//
//	@GetMapping
//	public String listProducts(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
//			@RequestParam(name = "size", defaultValue = "10") int size,
//			@RequestParam(name = "keyword", required = false) String keyword) {
//
//		Pageable pageable = PageRequest.of(page, size);
//		Page<ProductDTO> productPage;
//
//		if (keyword != null && !keyword.isEmpty()) {
//			productPage = productService.searchProducts(keyword, pageable);
//		} else {
//			productPage = productService.getAllProducts(pageable);
//		}
//
//		model.addAttribute("productPage", productPage);
//		model.addAttribute("keyword", keyword);
//		return "admin/product/list";
//	}
//
//	@GetMapping("/new")
//	public String showCreateForm(Model model) {
//		model.addAttribute("productDTO", new ProductDTO());
//		model.addAttribute("categories", categoryService.getAllCategories());
//		model.addAttribute("brands", brandService.getAllBrands());
//		model.addAttribute("shops", shopService.getAllShops());
//		return "admin/product/form";
//	}
//
//	@PostMapping
//	public String createProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO, BindingResult result,
//			@RequestParam("shopId") Integer shopId, Model model, RedirectAttributes redirectAttributes) {
//		if (result.hasErrors()) {
//			model.addAttribute("categories", categoryService.getAllCategories());
//			model.addAttribute("brands", brandService.getAllBrands());
//			model.addAttribute("shops", shopService.getAllShops());
//			return "admin/product/form";
//		}
//		try {
//			productService.createProduct(productDTO, shopId);
//			redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
//		} catch (Exception e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm!");
//		}
//		return "redirect:/admin/products";
//	}
//
//	@GetMapping("/edit/{id}")
//	public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			ProductDTO productDTO = productService.getProductById(id);
//			model.addAttribute("productDTO", productDTO);
//			model.addAttribute("categories", categoryService.getAllCategories());
//			model.addAttribute("brands", brandService.getAllBrands());
//			model.addAttribute("shops", shopService.getAllShops());
//			return "admin/product/form";
//		} catch (Exception e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
//			return "redirect:/admin/products";
//		}
//	}
//
//	@PostMapping("/edit/{id}")
//	public String updateProduct(@PathVariable Integer id, @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
//			BindingResult result, @RequestParam("shopId") Integer shopId, Model model,
//			RedirectAttributes redirectAttributes) {
//		if (result.hasErrors()) {
//			model.addAttribute("categories", categoryService.getAllCategories());
//			model.addAttribute("brands", brandService.getAllBrands());
//			model.addAttribute("shops", shopService.getAllShops());
//			return "admin/product/form";
//		}
//		try {
//			productService.updateProduct(id, productDTO, shopId);
//			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
//		} catch (Exception e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm!");
//		}
//		return "redirect:/admin/products";
//	}
//
//	@GetMapping("/delete/{id}")
//	public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
//		try {
//			productService.deleteProduct(id);
//			redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
//		} catch (Exception e) {
//			redirectAttributes.addFlashAttribute("errorMessage",
//					"Lỗi khi xóa sản phẩm! Sản phẩm có thể đã được liên kết với các dữ liệu khác.");
//		}
//		return "redirect:/admin/products";
//	}
//}
