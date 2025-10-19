package com.example.demo.service.impl;

import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.entity.Color;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductVariant;
import com.example.demo.entity.Size;
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
    private ProductService productService; // Injected to update parent product stock

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductVariantDTO createVariant(Integer productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductVariant variant = modelMapper.map(variantDTO, ProductVariant.class);
        variant.setProduct(product);

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
        productService.updateProductStock(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO getVariantById(Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
        return modelMapper.map(variant, ProductVariantDTO.class);
    }

    @Override
    @Transactional
    public void updateVariant(ProductVariantDTO variantDTO) {
        ProductVariant existingVariant = productVariantRepository.findById(variantDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantDTO.getId()));

        // Fetch and set Color
        Color color = colorRepository.findById(variantDTO.getColor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + variantDTO.getColor().getId()));
        existingVariant.setColor(color);

        // Fetch and set Size
        Size size = sizeRepository.findById(variantDTO.getSize().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + variantDTO.getSize().getId()));
        existingVariant.setSize(size);

        // Map other fields from DTO to the existing entity
        existingVariant.setPriceAdjustment(variantDTO.getPriceAdjustment());
        existingVariant.setStockQuantity(variantDTO.getStockQuantity());

        productVariantRepository.save(existingVariant);

        // Update the parent product's stock
        productService.updateProductStock(existingVariant.getProduct().getId());
    }
}
