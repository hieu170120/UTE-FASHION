package com.example.demo.service.impl;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.ProductService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public Page<ProductDTO> getProductsByCategorySlug(String slug, Pageable pageable) {
        return productRepository.findByCategorySlug(slug, pageable)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public Page<ProductDTO> getProductsByBrandSlug(String slug, Pageable pageable) {
        return productRepository.findByBrandSlug(slug, pageable)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setSlug(productDTO.getSlug());
        product.setDescription(productDTO.getLongDescription());
        product.setPrice(productDTO.getBasePrice());

        Category category = categoryRepository.findById(productDTO.getCategory().getId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategory().getId()));
        product.setCategory(category);

        Brand brand = brandRepository.findById(productDTO.getBrand().getId()).orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrand().getId()));
        product.setBrand(brand);

        if (productDTO.getImages() != null) {
            Set<ProductImage> images = productDTO.getImages().stream().map(imgDTO -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(imgDTO.getImageUrl());
                img.setProduct(product);
                return img;
            }).collect(Collectors.toSet());
            product.setImages(images);
        }

        if (productDTO.getVariants() != null) {
            Set<ProductVariant> variants = productDTO.getVariants().stream().map(varDTO -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setQuantity(varDTO.getQuantity());

                Size size = sizeRepository.findById(varDTO.getSize().getId()).orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + varDTO.getSize().getId()));
                variant.setSize(size);

                Color color = colorRepository.findById(varDTO.getColor().getId()).orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + varDTO.getColor().getId()));
                variant.setColor(color);
                return variant;
            }).collect(Collectors.toSet());
            product.setVariants(variants);
        }

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setSlug(productDTO.getSlug());
        existingProduct.setDescription(productDTO.getLongDescription());
        existingProduct.setPrice(productDTO.getBasePrice());

        Category category = categoryRepository.findById(productDTO.getCategory().getId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategory().getId()));
        existingProduct.setCategory(category);

        Brand brand = brandRepository.findById(productDTO.getBrand().getId()).orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrand().getId()));
        existingProduct.setBrand(brand);

        existingProduct.getImages().clear();
        if (productDTO.getImages() != null) {
            productDTO.getImages().forEach(imgDTO -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(imgDTO.getImageUrl());
                img.setProduct(existingProduct);
                existingProduct.getImages().add(img);
            });
        }

        existingProduct.getVariants().clear();
        if (productDTO.getVariants() != null) {
            productDTO.getVariants().forEach(varDTO -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(existingProduct);
                variant.setQuantity(varDTO.getQuantity());

                Size size = sizeRepository.findById(varDTO.getSize().getId()).orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + varDTO.getSize().getId()));
                variant.setSize(size);

                Color color = colorRepository.findById(varDTO.getColor().getId()).orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + varDTO.getColor().getId()));
                variant.setColor(color);
                existingProduct.getVariants().add(variant);
            });
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
