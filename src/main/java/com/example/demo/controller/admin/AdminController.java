package com.example.demo.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.AdminShopService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
public class AdminController {

	private final AdminShopService adminShopService;

	@GetMapping
	public String listShops(Model model) {
		model.addAttribute("shops", adminShopService.getAllShops());
		return "admin/shops/list";
	}

	@PostMapping("/approve/{shopId}")
	public String approveShop(@PathVariable("shopId") Integer shopId, RedirectAttributes redirectAttributes) {
		try {
			adminShopService.approveShop(shopId);
			redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt cửa hàng thành công.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
		}
		return "redirect:/admin/shops";
	}

	@PostMapping("/reject/{shopId}")
	public String rejectShop(@PathVariable("shopId") Integer shopId, RedirectAttributes redirectAttributes) {
		try {
			adminShopService.rejectShop(shopId);
			redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối/khóa cửa hàng thành công.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
		}
		return "redirect:/admin/shops";
	}
}
