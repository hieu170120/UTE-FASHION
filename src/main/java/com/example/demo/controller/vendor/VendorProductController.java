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
import com.example.demo.service.ColorService;
import com.example.demo.service.ProductService;
import com.example.demo.service.SizeService;
import com.example.demo.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
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

    @Autowired
    private ColorService colorService;

    @Autowired
    private SizeService sizeService;

    private static final int MAX_IMAGES = 5; // Increased to 5 to match the new uploader

    private void authorizeVendorForProduct(Principal principal, Integer productId) {
        Integer shopId = vendorService.getShopIdByUsername(principal.getName());
        ProductDTO product = productService.getProductById(productId);
        if (product == null || !product.getShopId().equals(shopId)) {
            throw new UnauthorizedException("Bạn không có quyền truy cập sản phẩm này.");
        }
    }

    @GetMapping
    public String showProductList(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Integer shopId = vendorService.getShopIdByUsername(principal.getName());
            List<ProductDTO> products = productService.getProductsByShopId(shopId);
            model.addAttribute("products", products);
            model.addAttribute("isProductListEmpty", products.isEmpty());
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần tạo một cửa hàng trước khi có thể quản lý sản phẩm.");
            return "redirect:/vendor/profile";
        }
        return "vendor/products/list";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            vendorService.getShopIdByUsername(principal.getName());
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy cửa hàng của bạn để thêm sản phẩm.");
            return "redirect:/vendor/products";
        }

        if (!model.containsAttribute("form")) {
             // We use ProductDTO directly now, not ProductFormDTO for adding
            model.addAttribute("form", new ProductFormDTO());
        }
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("brands", brandService.findAllActive());
        return "vendor/products/add";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("form") ProductFormDTO form, 
                             BindingResult result, 
                             @RequestParam(name = "imageUrls", required = false) List<String> imageUrls, // Capture image URLs
                             Principal principal, 
                             RedirectAttributes redirectAttributes, 
                             Model model) {
        Integer shopId;
        try {
            shopId = vendorService.getShopIdByUsername(principal.getName());
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy cửa hàng của bạn.");
            return "redirect:/vendor/products";
        }

        if (imageUrls == null || imageUrls.isEmpty()) {
            result.rejectValue("product.productName", "product.productName", "Vui lòng tải lên ít nhất một hình ảnh.");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            return "vendor/products/add";
        }

        try {
            ProductDTO productDTO = form.getProduct();

            // Convert URLs to ProductImageDTO list
            List<ProductImageDTO> images = new ArrayList<>();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (int i = 0; i < imageUrls.size(); i++) {
                    ProductImageDTO imageDTO = new ProductImageDTO();
                    imageDTO.setImageUrl(imageUrls.get(i));
                    imageDTO.setPrimary(i == 0); // First image is always primary
                    images.add(imageDTO);
                }
            }
            
            ProductDTO newProduct = productService.createProduct(productDTO, images, shopId);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công! Giờ bạn có thể thêm các biến thể.");
            return "redirect:/vendor/products/edit/" + newProduct.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi thêm sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/vendor/products/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            authorizeVendorForProduct(principal, id);

            ProductDTO productDTO = productService.getProductById(id);
            List<ProductImageDTO> images = productService.getImagesByProductId(id);

            // No need to pad the list anymore for the new uploader
            ProductFormDTO form = new ProductFormDTO();
            form.setProduct(productDTO);
            form.setImages(images); // Use the actual list of images

            model.addAttribute("form", form);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("variants", productService.getVariantsByProductId(id));
            model.addAttribute("newVariant", new ProductVariantDTO());

            model.addAttribute("allColors", colorService.findAll());
            model.addAttribute("allSizes", sizeService.findAll());

            return "vendor/products/edit";
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Integer id, @Valid @ModelAttribute("form") ProductFormDTO form, 
                                BindingResult result, 
                                @RequestParam(name = "imageUrls", required = false) List<String> imageUrls,
                                Principal principal, 
                                RedirectAttributes redirectAttributes, 
                                Model model) {
        try {
            authorizeVendorForProduct(principal, id);
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/products";
        }
        
        if (imageUrls == null || imageUrls.isEmpty()) {
            result.rejectValue("product.productName", "product.productName", "Vui lòng tải lên ít nhất một hình ảnh.");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("brands", brandService.findAllActive());
            model.addAttribute("variants", productService.getVariantsByProductId(id));
            model.addAttribute("newVariant", new ProductVariantDTO());
            model.addAttribute("form", form);
            model.addAttribute("allColors", colorService.findAll());
            model.addAttribute("allSizes", sizeService.findAll());
            return "vendor/products/edit";
        }

        try {
            Integer shopId = vendorService.getShopIdByUsername(principal.getName());
            ProductDTO productDTO = form.getProduct();

            // Process images from URLs
            List<ProductImageDTO> images = new ArrayList<>();
             if (imageUrls != null && !imageUrls.isEmpty()) {
                for (int i = 0; i < imageUrls.size(); i++) {
                    ProductImageDTO imageDTO = new ProductImageDTO();
                    imageDTO.setImageUrl(imageUrls.get(i));
                    imageDTO.setPrimary(i == 0); // First image is always primary
                    images.add(imageDTO);
                }
            }

            productService.updateProduct(id, productDTO, images, shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/vendor/products/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/vendor/products/edit/" + id;
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
