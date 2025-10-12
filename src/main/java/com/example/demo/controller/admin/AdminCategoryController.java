package com.example.demo.controller.admin;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/category/list"; // View: templates/admin/category/list.html
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/category/form"; // View: templates/admin/category/form.html
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category/form";
        }
        categoryService.createCategory(categoryDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            CategoryDTO categoryDTO = categoryService.getCategoryById(id);
            model.addAttribute("categoryDTO", categoryDTO);
            return "admin/category/form";
        } catch (Exception e) {
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Integer id,
                                 @Valid @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category/form";
        }
        categoryService.updateCategory(id, categoryDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa danh mục!");
        }
        return "redirect:/admin/categories";
    }
}
