package com.example.demo.service.impl;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductImageDTO;
import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- Main Public Methods for Fetching Product Lists ---

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProducts(Pageable pageable, String sort) {
        Page<ProductSummaryDTO> productPage;
        switch (sort) {
            case "bestseller":
                productPage = productRepository.findSummaryByOrderBySoldCountDesc(pageable);
                break;
            case "toprated":
                productPage = productRepository.findSummaryByOrderByAverageRatingDesc(pageable);
                break;
            case "mostwished":
                productPage = productRepository.findSummaryByOrderByWishlistCountDesc(pageable);
                break;
            case "newest":
            default:
                productPage = productRepository.findSummaryAll(pageable);
                break;
        }
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getProductsByCategory(String slug, Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByCategorySlug(slug, pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getProductsByBrand(String slug, Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByBrandSlug(slug, pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getProductsByShop(Integer shopId, Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByShopId(shopId, pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> searchProducts(String keyword, Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByProductNameContainingIgnoreCase(keyword, pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getBestsellerProducts() {
        List<ProductSummaryDTO> products = productRepository.findSummaryBestsellers(PageRequest.of(0, 8));
        loadImagesForProductSummaries(products);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getNewestProducts() {
        List<ProductSummaryDTO> products = productRepository.findSummaryNewest(PageRequest.of(0, 8));
        loadImagesForProductSummaries(products);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getBestsellerProducts(Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByOrderBySoldCountDesc(pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getTopRatedProducts(Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByOrderByAverageRatingDesc(pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getMostWishedProducts(Pageable pageable) {
        Page<ProductSummaryDTO> productPage = productRepository.findSummaryByOrderByWishlistCountDesc(pageable);
        loadImagesForProductSummaries(productPage.getContent());
        return productPage;
    }


    // --- Methods for Single, Detailed Product View ---

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return modelMapper.map(product, ProductDTO.class);
    }


    // --- CRUD Methods ---

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, Integer shopId) {
        Product product = modelMapper.map(productDTO, Product.class);
        product.setShop(shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO, Integer shopId) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        modelMapper.map(productDTO, existingProduct);
        existingProduct.setShop(shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));

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


    // --- Private Helper Methods ---

    private void loadImagesForProductSummaries(List<ProductSummaryDTO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        List<Integer> productIds = products.stream().map(ProductSummaryDTO::getId).collect(Collectors.toList());

        List<ProductImage> images = productImageRepository.findImagesByProductIds(productIds);

        Map<Integer, List<ProductImageDTO>> imagesByProductId = images.stream()
                .collect(Collectors.groupingBy(
                        image -> image.getProduct().getId(),
                        Collectors.mapping(image -> modelMapper.map(image, ProductImageDTO.class), Collectors.toList())
                ));

        products.forEach(p -> p.setImages(imagesByProductId.get(p.getId())));
    }
}
