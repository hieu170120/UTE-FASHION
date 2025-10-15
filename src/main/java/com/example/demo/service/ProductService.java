
package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.ProductDTO;

public interface ProductService {
	Page<ProductDTO> getAllProducts(Pageable pageable);

	ProductDTO getProductById(Integer id);

	ProductDTO getProductBySlug(String slug);

	Page<ProductDTO> getProductsByCategory(String slug, Pageable pageable);

	Page<ProductDTO> getProductsByBrand(String slug, Pageable pageable);
    
	Page<ProductDTO> getProductsByShop(Integer shopId, Pageable pageable);

	Page<ProductDTO> searchProducts(String keyword, Pageable pageable);

	ProductDTO createProduct(ProductDTO productDTO, Integer shopId);

	ProductDTO updateProduct(Integer id, ProductDTO productDTO, Integer shopId);

	void deleteProduct(Integer id);

}
