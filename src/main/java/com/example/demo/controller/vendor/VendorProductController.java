package com.example.demo.controller.vendor;

import com.example.demo.entity.User;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor/products")
@PreAuthorize("hasRole('VENDOR')")
@RequiredArgsConstructor
public class VendorProductController {

    private final ProductService productService;

    // Tạm thời, chúng ta sẽ hardcode user. Sau này sẽ được thay thế bằng interceptor hoặc Principal
    private User getCurrentUser() {
        User user = new User();
        user.setUserId(1); // Giả sử user có ID là 1 (admin/vendor)
        return user;
    }

    @GetMapping
    public String listProducts(Model model) {
        User currentUser = getCurrentUser();
        // Logic để lấy sản phẩm của vendor sẽ được thêm vào service
        // model.addAttribute("products", productService.getProductsByVendor(currentUser.getId()));
        return "vendor/products/list";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        // Logic để thêm các thuộc tính cần thiết cho form (như categories)
        // model.addAttribute("productDto", new ProductDto());
        // model.addAttribute("categories", categoryService.findAll());
        return "vendor/products/add";
    }
}
