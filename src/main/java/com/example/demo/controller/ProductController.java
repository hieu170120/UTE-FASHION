package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductSearchCriteria;
import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReviewService;
import com.example.demo.service.ShopService;

@Controller
public class ProductController {

    private static final List<String> ALLOWED_SORT_VALUES = List.of("newest", "bestseller", "averageRating", "wishList",
            "price-asc", "price-desc");
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    //	@Autowired
    //	private BrandService brandService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ShopService shopService;

    private void loadProductPage(ProductSearchCriteria criteria, int page, int size, Model model) {
        if (!StringUtils.hasText(criteria.getSort()) || !ALLOWED_SORT_VALUES.contains(criteria.getSort())) {
            criteria.setSort("newest");
        }
        Page<ProductSummaryDTO> productPage = productService.searchAndFilterProducts(criteria,
                PageRequest.of(page, size));
        model.addAttribute("productPage", productPage);
        model.addAttribute("criteria", criteria);
    }

    private ProductSearchCriteria createCriteriaFromParams(String categorySlug, String brandSlug, String keyword,
                                                           String sort) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategorySlug(categorySlug);
        criteria.setBrandSlug(brandSlug);
        criteria.setKeyword(keyword);
        criteria.setSort(sort);
        return criteria;
    }

    @GetMapping("/products")
    public String listAndFilterProducts(Model model, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String categorySlug,
                                        @RequestParam(required = false) String brandSlug, @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String sort) {

        ProductSearchCriteria criteria = createCriteriaFromParams(categorySlug, brandSlug, keyword, sort);

        String pageTitle = "Tất cả sản phẩm";
        if (StringUtils.hasText(criteria.getCategorySlug())) {
            try {
                CategoryDTO category = categoryService.getCategoryBySlug(criteria.getCategorySlug());
                pageTitle = category.getCategoryName();
            } catch (Exception e) {
                pageTitle = "Danh mục: " + criteria.getCategorySlug();
            }
        }

        loadProductPage(criteria, page, size, model);
        model.addAttribute("pageTitle", pageTitle);

        model.addAttribute("currentPage", page);
        model.addAttribute("currentCategory", criteria.getCategorySlug());
        model.addAttribute("currentBrand", criteria.getBrandSlug());
        model.addAttribute("keyword", criteria.getKeyword());
        model.addAttribute("sort", criteria.getSort());

        return "product/products";
    }

    @GetMapping("/products/fragments/list")
    public String getProductListFragment(Model model, @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String categorySlug,
                                         @RequestParam(required = false) String brandSlug, @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String sort) {
        ProductSearchCriteria criteria = createCriteriaFromParams(categorySlug, brandSlug, keyword, sort);
        loadProductPage(criteria, page, size, model);
        return "product/products :: productListFragment";
    }

    @GetMapping("/products/{slug}")
    public String viewProduct(@PathVariable String slug, Model model, Authentication authentication) {
        try {
            ProductDTO product = productService.findProductDetailBySlug(slug);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", product.getProductName());
            model.addAttribute("shop", shopService.getShopById(product.getShopId()));

            Integer currentUserId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof User) {
                    currentUserId = ((User) principal).getUserId();
                } else if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    currentUserId = userOpt.map(User::getUserId).orElse(null);
                } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                    String username = (String) principal;
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    currentUserId = userOpt.map(User::getUserId).orElse(null);
                }
            }
            model.addAttribute("currentUserId", currentUserId);

            if (currentUserId != null) {
                // Track the viewed product
                productService.trackViewedProduct(currentUserId, product.getId());

                // Get recently viewed products and add them to the model (excluding the current one)
                List<ProductSummaryDTO> recentlyViewed = productService.getRecentlyViewedProducts(currentUserId, product.getId(), 8);
                model.addAttribute("recentlyViewedProducts", recentlyViewed);
            }

            Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(product.getId(), PageRequest.of(0, 5));
            model.addAttribute("reviewPage", reviewPage);

            model.addAttribute("ratingStats", reviewService.getRatingStatistics(product.getId()));

            return "product/product-detail";
        } catch (Exception e) {
            return "redirect:/products?error=notfound";
        }
    }
}
