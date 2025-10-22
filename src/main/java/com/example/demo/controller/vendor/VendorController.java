package com.example.demo.controller.vendor;

import com.example.demo.dto.RevenueReportDTO;
import com.example.demo.dto.ShopRegistrationDTO;
import com.example.demo.entity.Shop;
import com.example.demo.service.RevenueReportService;
import com.example.demo.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final RevenueReportService revenueReportService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Kiểm tra xem user đã có shop chưa, nếu có thì chuyển hướng
        if (vendorService.getCurrentVendorShop().isPresent()) {
            return "redirect:/vendor/dashboard";
        }
        model.addAttribute("shopRegistrationDTO", new ShopRegistrationDTO());
        return "vendor/registration"; // Path to the registration form view
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("shopRegistrationDTO") ShopRegistrationDTO dto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/registration";
        }

        try {
            vendorService.registerNewShop(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký shop thành công! Vui lòng chờ quản trị viên phê duyệt.");
            return "redirect:/vendor/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/register";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            return "redirect:/vendor/register";
        }
        model.addAttribute("shop", shopOpt.get());
        return "vendor/dashboard"; // Path to the dashboard view
    }

    @GetMapping("/shop/edit")
    public String showEditShopForm(Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có cửa hàng để chỉnh sửa.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopOpt.get();
        ShopRegistrationDTO dto = new ShopRegistrationDTO();
        dto.setShopName(shop.getShopName());
        dto.setDescription(shop.getDescription());
        dto.setLogoUrl(shop.getLogoUrl());

        model.addAttribute("shopDTO", dto);
        return "vendor/shop_edit"; // Path to the shop edit form view
    }

    @PostMapping("/shop/edit")
    public String processEditShop(@Valid @ModelAttribute("shopDTO") ShopRegistrationDTO dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/shop_edit";
        }

        try {
            vendorService.updateCurrentVendorShop(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin shop thành công!");
            return "redirect:/vendor/dashboard";
        } catch (IllegalStateException | jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/shop/edit";
        }
    }

    @GetMapping("/report/revenue")
    public String showRevenueReport(@RequestParam(defaultValue = "week") String chartPeriod,
                                    @RequestParam(required = false) String chartDate,
                                    @RequestParam(defaultValue = "week") String statsPeriod,
                                    @RequestParam(defaultValue = "month") String categoryPeriod,
                                    @RequestParam(defaultValue = "week") String conversionPeriod,
                                    Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng để xem báo cáo.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopOpt.get();
        
        RevenueReportDTO report = revenueReportService.getRevenueReport(
                shop.getId(), chartPeriod, chartDate, statsPeriod, categoryPeriod, conversionPeriod);
        
        model.addAttribute("shop", shop);
        model.addAttribute("report", report);
        model.addAttribute("chartPeriod", chartPeriod);
        model.addAttribute("chartDate", chartDate != null ? chartDate : LocalDate.now().toString());
        model.addAttribute("statsPeriod", statsPeriod);
        model.addAttribute("categoryPeriod", categoryPeriod);
        model.addAttribute("conversionPeriod", conversionPeriod);
        return "vendor/revenue-report";
    }
}
