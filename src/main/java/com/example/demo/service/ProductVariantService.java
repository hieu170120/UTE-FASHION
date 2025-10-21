package com.example.demo.service;

import com.example.demo.dto.ProductVariantDTO;

public interface ProductVariantService {
    ProductVariantDTO createVariant(Integer productId, ProductVariantDTO variantDTO);
    void deleteVariant(Integer variantId);
    ProductVariantDTO getVariantById(Integer variantId);
    void updateVariant(ProductVariantDTO variantDTO);
}
