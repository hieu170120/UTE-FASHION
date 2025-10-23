package com.example.demo.controller.vendor;

import com.example.demo.dto.CouponDTO;
import com.example.demo.service.VendorCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vendor/coupons")
@RequiredArgsConstructor
public class VendorCouponController {
    
    private final VendorCouponService couponService;
    
    @GetMapping
    public String listCoupons(Model model) {
        List<CouponDTO> coupons = couponService.getAllVendorCoupons();
        model.addAttribute("coupons", coupons);
        return "vendor/coupon-list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("couponDTO", new CouponDTO());
        model.addAttribute("isEdit", false);
        return "vendor/coupon-form";
    }
    
    @PostMapping("/new")
    public String createCoupon(@Valid @ModelAttribute CouponDTO couponDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "vendor/coupon-form";
        }
        
        try {
            couponService.createCoupon(couponDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo coupon thành công!");
            return "redirect:/vendor/coupons";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            return "vendor/coupon-form";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CouponDTO coupon = couponService.getCouponById(id);
            model.addAttribute("couponDTO", coupon);
            model.addAttribute("isEdit", true);
            return "vendor/coupon-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/coupons";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateCoupon(@PathVariable Integer id,
                              @Valid @ModelAttribute CouponDTO couponDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            couponDTO.setId(id);
            return "vendor/coupon-form";
        }
        
        try {
            couponService.updateCoupon(id, couponDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật coupon thành công!");
            return "redirect:/vendor/coupons";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            couponDTO.setId(id);
            return "vendor/coupon-form";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCoupon(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa coupon thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vendor/coupons";
    }
    
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            couponService.toggleCouponStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái coupon thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vendor/coupons";
    }
}
