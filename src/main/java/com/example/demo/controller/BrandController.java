package com.example.demo.controller;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.BrandService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @GetMapping("/{slug}")
    public String viewBrandProducts(@PathVariable String slug, Model model,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "12") int size) {
        BrandDTO brand = brandService.getBrandBySlug(slug);
        Page<ProductDTO> products = productService.getProductsByBrandSlug(slug, PageRequest.of(page, size));

        model.addAttribute("pageTitle", "Sản phẩm thuộc thương hiệu " + brand.getBrandName());
        model.addAttribute("brand", brand);
        model.addAttribute("products", products);

        return "product/list"; // Tái sử dụng view list sản phẩm
    }
}
