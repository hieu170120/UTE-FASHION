package com.example.demo.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductImageDTO;
import com.example.demo.dto.ProductSearchCriteria;
import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.entity.ProductVariant;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.specification.ProductSpecification;
import com.example.demo.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository productImageRepository;
	@Autowired
	private ProductVariantRepository productVariantRepository;
	@Autowired
	private ShopRepository shopRepository;
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> searchAndFilterProducts(ProductSearchCriteria criteria, Pageable pageable) {
		Specification<Product> spec = ProductSpecification.fromCriteria(criteria);
		Pageable adjustedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				getSortOrder(criteria.getSort()));

		// OPTIMIZED: Call the custom repository method that uses DTO projection.
		// This is much more efficient than findAll() and mapping afterwards.
		return productRepository.findSummaries(spec, adjustedPageable);
	}

	private Sort getSortOrder(String sort) {
		switch (sort) {
		case "newest":
			return Sort.by("createdAt").descending();
		case "bestseller":
			return Sort.by("soldCount").descending();
		case "toprated":
			return Sort.by("averageRating").descending();
		case "price-asc":
			return Sort.by("salePrice").ascending();
		case "price-desc":
			return Sort.by("salePrice").descending();
		case "wishList":
		default:
			return Sort.by("createdAt").descending();
		}
	}

	@Override
	@Async
	@Transactional(readOnly = true)
	public CompletableFuture<List<ProductSummaryDTO>> getBestsellerProducts() {
		return CompletableFuture.completedFuture(productRepository.findSummaryBestsellers(PageRequest.of(0, 8)));
	}

	@Override
	@Async
	@Transactional(readOnly = true)
	public CompletableFuture<List<ProductSummaryDTO>> getNewestProducts() {
		return CompletableFuture.completedFuture(productRepository.findSummaryNewest(PageRequest.of(0, 8)));
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO findProductDetailBySlug(String slug) {
		Product product = productRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO getProductBySlug(String slug) {
		Product product = productRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO getProductById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> findEligibleOrdersForReview(Integer userId, Integer productId) {
		return orderRepository.findEligibleOrdersForReview(userId, productId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductImageDTO> getImagesByProductId(Integer productId) {
		List<ProductImage> images = productImageRepository.findAllByProductId(productId);
		return images.stream().map(image -> modelMapper.map(image, ProductImageDTO.class)).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariantDTO> getVariantsByProductId(Integer productId) {
		List<ProductVariant> variants = productVariantRepository.findWithDetailsByProductId(productId);
		return variants.stream().map(variant -> modelMapper.map(variant, ProductVariantDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Integer, List<ProductImageDTO>> getImagesForProducts(List<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<ProductImage> images = productImageRepository.findTop2ImagesPerProduct(productIds);
		return images.stream().collect(Collectors.groupingBy(image -> image.getProduct().getId(),
				Collectors.mapping(image -> modelMapper.map(image, ProductImageDTO.class), Collectors.toList())));
	}

	@Override
	@Transactional
	public ProductDTO createProduct(ProductDTO productDTO, Integer shopId) {
		Product product = modelMapper.map(productDTO, Product.class);
		product.setShop(
				shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));
		Product savedProduct = productRepository.save(product);
		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	@Transactional
	public ProductDTO updateProduct(Integer id, ProductDTO productDTO, Integer shopId) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		modelMapper.map(productDTO, existingProduct);
		existingProduct.setShop(
				shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));

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

	@Override
	@Transactional(readOnly = true)
	public List<Product> getProductsByShopId(Integer shopId) {
		return productRepository.findAllByShop_ShopId(shopId);
	}

    @Override
    @Transactional
    public void updateProductStock(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Calculate the total stock from all its variants
        int totalStock = product.getVariants().stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();

        // Update the parent product's stock
        product.setStock(totalStock);

        productRepository.save(product);
    }
}
