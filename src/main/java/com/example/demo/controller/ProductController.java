
package com.example.demo.controller;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.BrandService;
import com.example.demo.service.CategoryService;
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

import java.util.List;

/**
 * Controller cho các trang sản phẩm phía người dùng
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * Hiển thị trang danh sách tất cả sản phẩm có phân trang và tìm kiếm
     */
    @GetMapping
    public String listAllProducts(Model model,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "9") int size,
                                  @RequestParam(name = "keyword", required = false) String keyword) {

        Page<ProductDTO> productPage;
        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchProducts(keyword, PageRequest.of(page, size));
            model.addAttribute("pageTitle", "Kết quả tìm kiếm cho '" + keyword + "'");
        } else {
            productPage = productService.getAllProducts(PageRequest.of(page, size));
            model.addAttribute("pageTitle", "Tất cả sản phẩm");
        }

        List<CategoryDTO> categories = categoryService.getAllCategories();
        List<BrandDTO> brands = brandService.getAllBrands();


        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);

        return "product/list";
    }
    
    @GetMapping("/category/{categorySlug}")
    public String listProductsByCategory(Model model,
                                         @PathVariable String categorySlug,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "9") int size) {

        Page<ProductDTO> productPage = productService.getProductsByCategory(categorySlug, PageRequest.of(page, size));
        CategoryDTO category = categoryService.getCategoryBySlug(categorySlug);
        List<CategoryDTO> categories = categoryService.getAllCategories();
        List<BrandDTO> brands = brandService.getAllBrands();

        model.addAttribute("productPage", productPage);
        model.addAttribute("pageTitle", "Sản phẩm thuộc danh mục '" + category.getName() + "'");
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        return "product/list";
    }

    @GetMapping("/brand/{brandSlug}")
    public String listProductsByBrand(Model model,
                                      @PathVariable String brandSlug,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "9") int size) {
        
        Page<ProductDTO> productPage = productService.getProductsByBrand(brandSlug, PageRequest.of(page, size));
        BrandDTO brand = brandService.getBrandBySlug(brandSlug);
        List<CategoryDTO> categories = categoryService.getAllCategories();
        List<BrandDTO> brands = brandService.getAllBrands();

        model.addAttribute("productPage", productPage);
        model.addAttribute("pageTitle", "Sản phẩm thuộc thương hiệu '" + brand.getName() + "'");
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);

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
