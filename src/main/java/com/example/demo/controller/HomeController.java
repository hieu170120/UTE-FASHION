package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        // Get 8 newest products
        List<ProductDTO> newestProducts = productService.getAllProducts(PageRequest.of(0, 8)).getContent();
        model.addAttribute("newestProducts", newestProducts);

        // Get 8 bestseller products
        List<ProductDTO> bestsellerProducts = productService.getBestsellerProducts();
        model.addAttribute("bestsellerProducts", bestsellerProducts);

        return "home";
    }
}
