package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> getAllProducts(Pageable pageable);
    ProductDTO getProductById(Integer id);
    ProductDTO getProductBySlug(String slug);
    Page<ProductDTO> getProductsByCategorySlug(String slug, Pageable pageable);
    Page<ProductDTO> getProductsByBrandSlug(String slug, Pageable pageable);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Integer id, ProductDTO productDTO);
    void deleteProduct(Integer id);
}
