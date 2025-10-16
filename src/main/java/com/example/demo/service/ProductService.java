package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductSummaryDTO> getAllProducts(Pageable pageable, String sort);

    ProductDTO getProductById(Integer id);

    ProductDTO getProductBySlug(String slug);

    Page<ProductSummaryDTO> getProductsByCategory(String slug, Pageable pageable);

    Page<ProductSummaryDTO> getProductsByBrand(String slug, Pageable pageable);

    Page<ProductSummaryDTO> getProductsByShop(Integer shopId, Pageable pageable);

    Page<ProductSummaryDTO> searchProducts(String keyword, Pageable pageable);

    List<ProductSummaryDTO> getBestsellerProducts();

    List<ProductSummaryDTO> getNewestProducts();

    ProductDTO createProduct(ProductDTO productDTO, Integer shopId);

    ProductDTO updateProduct(Integer id, ProductDTO productDTO, Integer shopId);

    void deleteProduct(Integer id);

    Page<ProductSummaryDTO> getBestsellerProducts(Pageable pageable);

    Page<ProductSummaryDTO> getTopRatedProducts(Pageable pageable);

    Page<ProductSummaryDTO> getMostWishedProducts(Pageable pageable);
}
