package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ReviewService reviewService; // Assuming you have a ReviewService

    private void addCommonAttributes(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("shops", shopService.getAllShops());
    }

    @GetMapping
    public String listAllProducts(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "20") int size,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @RequestParam(name = "sort", defaultValue = "newest") String sort) {

        Page<ProductSummaryDTO> productPage;
        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchProducts(keyword, PageRequest.of(page, size));
            model.addAttribute("pageTitle", "Kết quả tìm kiếm cho '" + keyword + "'");
        } else {
            productPage = productService.getAllProducts(PageRequest.of(page, size), sort);
            model.addAttribute("pageTitle", "Tất cả sản phẩm");
        }

        addCommonAttributes(model);
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "product/products";
    }

    @GetMapping("/category/{categorySlug}")
    public String listProductsByCategory(Model model, @PathVariable String categorySlug,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<ProductSummaryDTO> productPage = productService.getProductsByCategory(categorySlug, PageRequest.of(page, size));
        CategoryDTO category = categoryService.getCategoryBySlug(categorySlug);

        addCommonAttributes(model);
        model.addAttribute("productPage", productPage);
        model.addAttribute("pageTitle", "Sản phẩm thuộc danh mục '" + category.getCategoryName() + "'");
        model.addAttribute("currentCategory", categorySlug);
        return "product/products";
    }

    @GetMapping("/brand/{brandSlug}")
    public String listProductsByBrand(Model model, @PathVariable String brandSlug,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<ProductSummaryDTO> productPage = productService.getProductsByBrand(brandSlug, PageRequest.of(page, size));
        BrandDTO brand = brandService.getBrandBySlug(brandSlug);

        addCommonAttributes(model);
        model.addAttribute("productPage", productPage);
        model.addAttribute("pageTitle", "Sản phẩm thuộc thương hiệu '" + brand.getBrandName() + "'");
        model.addAttribute("currentBrand", brandSlug);
        return "product/products";
    }

    @GetMapping("/shop/{shopId}")
    public String listProductsByShop(Model model, @PathVariable Integer shopId,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<ProductSummaryDTO> productPage = productService.getProductsByShop(shopId, PageRequest.of(page, size));
        ShopDTO shop = shopService.getShopById(shopId);

        addCommonAttributes(model);
        model.addAttribute("productPage", productPage);
        model.addAttribute("pageTitle", "Sản phẩm của shop '" + shop.getShopName() + "'");
        model.addAttribute("currentShop", shopId);
        return "product/products";
    }


    @GetMapping("/{slug}")
    public String viewProduct(@PathVariable String slug, Model model, @AuthenticationPrincipal User currentUser) {
        try {
            // Single, efficient call to the service layer
            ProductDTO productDTO = productService.findProductDetailBySlug(slug);
            model.addAttribute("product", productDTO);
            model.addAttribute("pageTitle", productDTO.getProductName());

            // The DTO from the optimized service method now contains variants, brand, etc.
            // No need for extra repository/service calls here.
            model.addAttribute("brand", productDTO.getBrandId());
            model.addAttribute("variants", productDTO.getVariants());

            // Fetch eligible orders for review (this logic is separate and okay to keep)
            List<Order> eligibleOrders = Collections.emptyList();
            if (currentUser != null) {
                // Pass the user ID and product ID to the service
                eligibleOrders = productService.findEligibleOrdersForReview(currentUser.getUserId(), productDTO.getId());
            }
            model.addAttribute("eligibleOrders", eligibleOrders);

            // Fetch reviews for the product
            Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(productDTO.getId(), PageRequest.of(0, 5)); // Example: get first 5 reviews
            model.addAttribute("reviewPage", reviewPage);

            return "product/product-detail";
        } catch (Exception e) {
            // Proper logging should be implemented here
            e.printStackTrace();
            return "redirect:/products?error=notfound";
        }
    }
}
