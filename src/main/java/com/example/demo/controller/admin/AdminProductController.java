package com.example.demo.controller.admin;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showProductList(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/list";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActiveStatus(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleProductActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
