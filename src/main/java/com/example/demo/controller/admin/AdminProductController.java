package com.example.demo.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.BrandService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	@Autowired
	private ProductService productService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private BrandService brandService;

	@GetMapping
	public String listProducts(Model model) {
//		model.addAttribute("products", productService.getAllProducts());
		return "admin/product/list"; // View: templates/admin/product/list.html
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("productDTO", new ProductDTO());
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
		return "admin/product/form"; // View: templates/admin/product/form.html
	}

	@PostMapping
	public String createProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("categories", categoryService.getAllCategories());
			model.addAttribute("brands", brandService.getAllBrands());
			return "admin/product/form";
		}
		productService.createProduct(productDTO);
		redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
		return "redirect:/admin/products";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Integer id, Model model) {
		try {
			ProductDTO productDTO = productService.getProductById(id);
			model.addAttribute("productDTO", productDTO);
			model.addAttribute("categories", categoryService.getAllCategories());
			model.addAttribute("brands", brandService.getAllBrands());
			return "admin/product/form";
		} catch (Exception e) {
			return "redirect:/admin/products";
		}
	}

	@PostMapping("/edit/{id}")
	public String updateProduct(@PathVariable Integer id, @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("categories", categoryService.getAllCategories());
			model.addAttribute("brands", brandService.getAllBrands());
			return "admin/product/form";
		}
		productService.updateProduct(id, productDTO);
		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
		return "redirect:/admin/products";
	}

	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		try {
			productService.deleteProduct(id);
			redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm!");
		}
		return "redirect:/admin/products";
	}
}
