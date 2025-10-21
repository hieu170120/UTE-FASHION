package com.example.demo.service.impl;

import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.entity.Color;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductVariant;
import com.example.demo.entity.Size;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ColorRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.SizeRepository;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductVariantService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductVariantDTO createVariant(Integer productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // --- SKU Uniqueness Check ---
        if (StringUtils.hasText(variantDTO.getSku())) {
            productVariantRepository.findBySkuIgnoreCase(variantDTO.getSku()).ifPresent(v -> {
                throw new DuplicateResourceException("SKU '" + variantDTO.getSku() + "' đã tồn tại. Vui lòng chọn một SKU khác.");
            });
        }

        Color color = (variantDTO.getColor() != null && variantDTO.getColor().getId() != null) ?
                colorRepository.findById(variantDTO.getColor().getId()).orElse(null) : null;
        Size size = (variantDTO.getSize() != null && variantDTO.getSize().getId() != null) ?
                sizeRepository.findById(variantDTO.getSize().getId()).orElse(null) : null;

        // --- Combination Uniqueness Check ---
        productVariantRepository.findByProductAndColorAndSize(product, color, size).ifPresent(v -> {
            throw new DuplicateResourceException("Một biến thể với màu sắc và kích thước này đã tồn tại cho sản phẩm.");
        });

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(variantDTO.getSku());
        variant.setPriceAdjustment(variantDTO.getPriceAdjustment());
        variant.setStockQuantity(variantDTO.getStockQuantity());
        variant.setActive(true);
        variant.setColor(color);
        variant.setSize(size);

        ProductVariant savedVariant = productVariantRepository.save(variant);
        productService.updateProductStock(productId);

        return modelMapper.map(savedVariant, ProductVariantDTO.class);
    }

    @Override
    @Transactional
    public void deleteVariant(Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        Integer productId = variant.getProduct().getId();
        productVariantRepository.delete(variant);
        productVariantRepository.flush();
        productService.updateProductStock(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO getVariantById(Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
        // ModelMapper correctly maps the variant's own ID to the DTO's ID.
        // The problematic if-block that overwrote the ID has been removed.
        return modelMapper.map(variant, ProductVariantDTO.class);
    }

    @Override
    @Transactional
    public void updateVariant(ProductVariantDTO variantDTO) {
        ProductVariant existingVariant = productVariantRepository.findById(variantDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantDTO.getId()));

        // --- SKU Uniqueness Check ---
        if (StringUtils.hasText(variantDTO.getSku())) {
            Optional<ProductVariant> variantWithSameSku = productVariantRepository.findBySkuIgnoreCase(variantDTO.getSku());
            if (variantWithSameSku.isPresent() && !variantWithSameSku.get().getId().equals(existingVariant.getId())) {
                throw new DuplicateResourceException("SKU '" + variantDTO.getSku() + "' đã được sử dụng bởi một biến thể khác.");
            }
        }

        Color color = (variantDTO.getColor() != null && variantDTO.getColor().getId() != null) ?
                colorRepository.findById(variantDTO.getColor().getId()).orElse(null) : null;
        Size size = (variantDTO.getSize() != null && variantDTO.getSize().getId() != null) ?
                sizeRepository.findById(variantDTO.getSize().getId()).orElse(null) : null;

        // --- Combination Uniqueness Check ---
        Optional<ProductVariant> duplicateCombination = productVariantRepository.findByProductAndColorAndSize(existingVariant.getProduct(), color, size);
        if (duplicateCombination.isPresent() && !duplicateCombination.get().getId().equals(existingVariant.getId())) {
            throw new DuplicateResourceException("Một biến thể khác với màu sắc và kích thước này đã tồn tại cho sản phẩm.");
        }

        existingVariant.setSku(variantDTO.getSku());
        existingVariant.setPriceAdjustment(variantDTO.getPriceAdjustment());
        existingVariant.setStockQuantity(variantDTO.getStockQuantity());
        existingVariant.setColor(color);
        existingVariant.setSize(size);

        productService.updateProductStock(existingVariant.getProduct().getId());
    }
}
