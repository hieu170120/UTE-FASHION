package com.example.demo.controller.vendor;

import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductVariantService;
import com.example.demo.service.VendorService;
import com.example.demo.service.ColorService;
import com.example.demo.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/vendor/products/{productId}/variants")
public class VendorVariantController {

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private SizeService sizeService;

    private void authorizeVendorForProduct(Principal principal, Integer productId) {
        Integer shopId = vendorService.getShopIdByUsername(principal.getName());
        if (shopId == null || !productService.getProductById(productId).getShopId().equals(shopId)) {
            throw new UnauthorizedException("Bạn không có quyền thực hiện hành động này.");
        }
    }

    @PostMapping("/add")
    public String addVariant(@PathVariable("productId") Integer productId,
                             @Valid @ModelAttribute("newVariant") ProductVariantDTO variantDTO,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, productId);
            productVariantService.createVariant(productId, variantDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm biến thể: " + e.getMessage());
        }
        return "redirect:/vendor/products/edit/" + productId;
    }

    @GetMapping("/edit/{variantId}")
    public String showEditVariantForm(@PathVariable("productId") Integer productId,
                                      @PathVariable("variantId") Integer variantId,
                                      Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, productId);
            ProductVariantDTO variantDTO = productVariantService.getVariantById(variantId);
            model.addAttribute("variant", variantDTO);
            model.addAttribute("productId", productId);
            model.addAttribute("allColors", colorService.findAll());
            model.addAttribute("allSizes", sizeService.findAll());
            return "vendor/products/editVariant";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tìm thấy biến thể.");
            return "redirect:/vendor/products/edit/" + productId;
        }
    }

    @PostMapping("/edit/{variantId}")
    public String updateVariant(@PathVariable("productId") Integer productId,
                                @PathVariable("variantId") Integer variantId,
                                @Valid @ModelAttribute("variant") ProductVariantDTO variantDTO,
                                Principal principal, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, productId);
            variantDTO.setId(variantId); // Ensure the ID is set for update
            productVariantService.updateVariant(variantDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật biến thể: " + e.getMessage());
        }
        return "redirect:/vendor/products/edit/" + productId;
    }

    @GetMapping("/delete/{variantId}")
    public String deleteVariant(@PathVariable("productId") Integer productId,
                                @PathVariable("variantId") Integer variantId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, productId);
            productVariantService.deleteVariant(variantId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa biến thể: " + e.getMessage());
        }
        return "redirect:/vendor/products/edit/" + productId;
    }
}
