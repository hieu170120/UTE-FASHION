package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ProductService {

    // --- Consolidated Search and Filter Method ---
    Page<ProductSummaryDTO> searchAndFilterProducts(ProductSearchCriteria criteria, Pageable pageable);

    // --- Detailed Product View ---
    ProductDTO getProductById(Integer id);

    ProductDTO getProductBySlug(String slug);

    ProductDTO findProductDetailBySlug(String slug);

    // --- Homepage/Specific Lists ---
    CompletableFuture<List<ProductSummaryDTO>> getBestsellerProducts();

    CompletableFuture<List<ProductSummaryDTO>> getNewestProducts();

    // --- CRUD Operations ---
    ProductDTO createProduct(ProductDTO productDTO, List<ProductImageDTO> images, Integer shopId);

    ProductDTO updateProduct(Integer id, ProductDTO productDTO, List<ProductImageDTO> images, Integer shopId);

    void deleteProduct(Integer id);

    // --- Vendor Specific ---
    List<ProductDTO> getProductsByShopId(Integer shopId);

    // --- Review and Order Related ---
    List<Order> findEligibleOrdersForReview(Integer userId, Integer productId);

    // --- API Methods for Lazy Loading ---
    List<ProductImageDTO> getImagesByProductId(Integer productId);

    List<ProductVariantDTO> getVariantsByProductId(Integer productId);

    Map<Integer, List<ProductImageDTO>> getImagesForProducts(List<Integer> productIds);

    // --- Stock Management ---
    void updateProductStock(Integer productId);

    // --- Admin specific ---
    List<ProductDTO> getAllProducts();

    void toggleProductActiveStatus(Integer id);
}
