package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductSearchCriteria;
import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/products")
    public String listAndFilterProducts(@ModelAttribute ProductSearchCriteria criteria,
                                        Model model,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<ProductSummaryDTO> productPage = productService.searchAndFilterProducts(criteria, PageRequest.of(page, size));

        model.addAttribute("productPage", productPage);
        model.addAttribute("criteria", criteria); 
        model.addAttribute("pageTitle", "Danh sách sản phẩm"); 

        return "product/products";
    }

    @GetMapping("/products/{slug}")
    public String viewProduct(@PathVariable String slug, Model model) {
        try {
            ProductDTO product = productService.findProductDetailBySlug(slug);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", product.getProductName());

            Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(product.getId(), PageRequest.of(0, 5));
            model.addAttribute("reviewPage", reviewPage);

            return "product/product-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/products?error=notfound";
        }
    }
}
