package com.example.demo.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.service.AdminShopService;
import com.example.demo.service.ShopService;
import com.example.demo.dto.CommissionRequest;
import com.example.demo.dto.ShopDTO;

import lombok.Data;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/shops")
public class AdminController {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private AdminShopService adminShopService;
	
	@Autowired
	private ShopService shopService;

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
	
	/**
	 * API cập nhật chiết khấu cho shop
	 * PUT /admin/shops/{id}/commission
	 */
	@PutMapping(value = "/{id}/commission", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> updateShopCommission(@PathVariable("id") Integer shopId, 
	                                               @RequestBody CommissionRequest request) {
		logger.info("🔵 [AdminController] updateShopCommission - START");
		logger.info("   shopId: {}", shopId);
		logger.info("   commissionPercentage: {}", request.getCommissionPercentage());
		
		try {
			if (request.getCommissionPercentage() == null) {
				logger.warn("⚠️ [AdminController] Commission percentage is NULL");
				return ResponseEntity.badRequest().body(new ApiResponse("error", "Chiết khấu không được để trống", null));
			}
			
			logger.info("✅ [AdminController] Calling shopService.updateShopCommission()");
			ShopDTO updatedShop = shopService.updateShopCommission(shopId, request.getCommissionPercentage());
			
			logger.info("✅ [AdminController] Commission updated successfully");
			logger.info("   Updated shop: id={}, commission={}", updatedShop.getId(), updatedShop.getCommissionPercentage());
			
			return ResponseEntity.ok(new ApiResponse("success", "Cập nhật chiết khấu thành công", updatedShop));
		} catch (IllegalArgumentException e) {
			logger.error("❌ [AdminController] IllegalArgumentException: {}", e.getMessage());
			logger.error("   Error details:", e);
			return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage(), null));
		} catch (Exception e) {
			logger.error("❌ [AdminController] Unexpected Exception: {}", e.getMessage());
			logger.error("   Full stack trace:", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse("error", "Lỗi: " + e.getMessage(), null));
		}
	}
	
	// Helper class for API response
	@Data
	@AllArgsConstructor
	public static class ApiResponse {
		private String status;
		private String message;
		private Object data;
		
		public ApiResponse(String status, String message) {
			this.status = status;
			this.message = message;
			this.data = null;
		}
	}
}
