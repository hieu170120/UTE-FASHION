package com.example.demo.controller.vendor;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductFormDTO;
import com.example.demo.dto.ProductImageDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.entity.Product;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.service.BrandService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendor/products")
public class VendorProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    private void authorizeVendorForProduct(Principal principal, Integer productId) {
        Integer shopId = vendorService.getShopIdByUsername(principal.getName());
        ProductDTO product = productService.getProductById(productId);
        if (!product.getShopId().equals(shopId)) {
            throw new UnauthorizedException("Bạn không có quyền thực hiện hành động này trên sản phẩm này.");
        }
    }

    @GetMapping
    public String showProductList(Model model, Principal principal) {
        try {
            Integer shopId = vendorService.getShopIdByUsername(principal.getName());
            List<Product> products = productService.getProductsByShopId(shopId);
            model.addAttribute("products", products);
            model.addAttribute("isProductListEmpty", products.isEmpty());
        } catch (UnauthorizedException e) {
            model.addAttribute("errorMessage", "Bạn chưa có cửa hàng. Vui lòng tạo cửa hàng trước.");
            model.addAttribute("isProductListEmpty", true);
        }

        return "vendor/products/list";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            vendorService.getShopIdByUsername(principal.getName());
            model.addAttribute("form", new ProductFormDTO());
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            return "vendor/products/add";
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy cửa hàng của bạn.");
            return "redirect:/vendor/products";
        }
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("form") ProductFormDTO form, BindingResult result, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        Integer shopId;
        try {
            shopId = vendorService.getShopIdByUsername(principal.getName());
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy cửa hàng của bạn.");
            return "redirect:/vendor/products";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            return "vendor/products/add";
        }

        try {
            ProductDTO productDTO = form.getProduct();

            List<ProductImageDTO> images = form.getImages().stream()
                .filter(img -> img.getImageUrl() != null && !img.getImageUrl().trim().isEmpty())
                .collect(Collectors.toList());

            Integer primaryIndex = form.getPrimaryImageIndex();
            if (primaryIndex != null && primaryIndex >= 0 && primaryIndex < images.size()) {
                images.get(primaryIndex).setPrimary(true);
            }

            ProductDTO newProduct = productService.createProduct(productDTO, images, shopId);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công! Giờ bạn có thể thêm các biến thể.");
            return "redirect:/vendor/products/edit/" + newProduct.getId();
        } catch (Exception e) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi thêm sản phẩm: " + e.getMessage());
            return "vendor/products/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, id);

            ProductDTO productDTO = productService.getProductById(id);
            List<ProductImageDTO> images = productService.getImagesByProductId(id);
            List<CategoryDTO> categories = categoryService.findAllActive();
            List<BrandDTO> brands = brandService.findAllActive();
            List<ProductVariantDTO> variants = productService.getVariantsByProductId(id);

            ProductFormDTO form = new ProductFormDTO();
            form.setProduct(productDTO);
            form.setImages(images);

            model.addAttribute("form", form);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("variants", variants);
            model.addAttribute("newVariant", new ProductVariantDTO());

            return "vendor/products/edit";
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm hoặc đã có lỗi xảy ra.");
            return "redirect:/vendor/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Integer id, @Valid @ModelAttribute("productDTO") ProductDTO productDTO, BindingResult result, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, id);
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/products";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            return "vendor/products/edit";
        }

        try {
            Integer shopId = vendorService.getShopIdByUsername(principal.getName());
            productService.updateProduct(id, productDTO, shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/vendor/products/edit/" + id;
        } catch (Exception e) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "vendor/products/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, id);
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/vendor/products";
    }
}
