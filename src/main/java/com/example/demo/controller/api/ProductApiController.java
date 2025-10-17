package com.example.demo.controller.api;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    /**
     * API for LAZY LOADING: Gets ALL images for a SINGLE product (for product detail page).
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Integer productId) {
        // This correctly calls the service method that uses findAllByProductId
        List<ProductImageDTO> images = productService.getImagesByProductId(productId);
        return ResponseEntity.ok(images);
    }

    /**
     * API for LAZY LOADING: Gets ALL variants for a SINGLE product (for product detail page).
     */
    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<ProductVariantDTO>> getProductVariants(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = productService.getVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    /**
     * API for BATCH LOADING: Efficiently gets the TOP 2 images for a LIST of products (for product list/grid pages).
     * This is the new endpoint we are adding.
     */
    @PostMapping("/images-for-list")
    public ResponseEntity<Map<Integer, List<ProductImageDTO>>> getImagesForProductList(@RequestBody List<Integer> productIds) {
        // This correctly calls the service method that uses the OPTIMIZED findTop2ImagesPerProduct repository method.
        Map<Integer, List<ProductImageDTO>> imagesMap = productService.getImagesForProducts(productIds);
        return ResponseEntity.ok(imagesMap);
    }
}
