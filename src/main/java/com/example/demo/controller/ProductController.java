package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller cho các trang sản phẩm phía người dùng
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Hiển thị trang danh sách tất cả sản phẩm có phân trang
     */
    @GetMapping
    public String listAllProducts(Model model, 
                                  @RequestParam(name = "page", defaultValue = "0") int page, 
                                  @RequestParam(name = "size", defaultValue = "12") int size) {
        Page<ProductDTO> productPage = productService.getAllProducts(PageRequest.of(page, size));
        model.addAttribute("pageTitle", "Tất cả sản phẩm");
        model.addAttribute("productPage", productPage);
        return "product/list";
    }

    /**
     * Hiển thị trang chi tiết sản phẩm bằng slug
     */
    @GetMapping("/{slug}")
    public String viewProduct(@PathVariable String slug, Model model) {
        try {
            ProductDTO product = productService.getProductBySlug(slug);
            model.addAttribute("pageTitle", product.getProductName());
            model.addAttribute("product", product);
            return "product/detail";
        } catch (Exception e) {
            return "redirect:/products";
        }
    }
}
