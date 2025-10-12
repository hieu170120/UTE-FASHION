package com.example.demo.controller.admin;

import com.example.demo.dto.BrandDTO;
import com.example.demo.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/brands")
public class AdminBrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        return "admin/brand/list"; // View: templates/admin/brand/list.html
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("brandDTO", new BrandDTO());
        return "admin/brand/form"; // View: templates/admin/brand/form.html
    }

    @PostMapping
    public String createBrand(@Valid @ModelAttribute("brandDTO") BrandDTO brandDTO,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/brand/form";
        }
        brandService.createBrand(brandDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu thành công!");
        return "redirect:/admin/brands";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            BrandDTO brandDTO = brandService.getBrandById(id);
            model.addAttribute("brandDTO", brandDTO);
            return "admin/brand/form";
        } catch (Exception e) {
            return "redirect:/admin/brands";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBrand(@PathVariable Integer id,
                              @Valid @ModelAttribute("brandDTO") BrandDTO brandDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/brand/form";
        }
        brandService.updateBrand(id, brandDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công!");
        return "redirect:/admin/brands";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa thương hiệu!");
        }
        return "redirect:/admin/brands";
    }
}
