package com.example.demo.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
import com.example.demo.repository.BrandRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.specification.ProductSpecification;
import com.example.demo.service.ProductService;
import com.github.slugify.Slugify;

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
	private CategoryRepository categoryRepository;
	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private Slugify slugify;

	private ProductDTO convertToDto(Product product) {
		ProductDTO dto = modelMapper.map(product, ProductDTO.class);
		if (product.getShop() != null) {
			dto.setShopId(product.getShop().getId());
			dto.setShopName(product.getShop().getShopName());
		}
		if (product.getCategory() != null) {
			dto.setCategoryId(product.getCategory().getId());
		}
		if (product.getBrand() != null) {
			dto.setBrandId(product.getBrand().getId());
			dto.setBrandName(product.getBrand().getBrandName());
		}
		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> searchAndFilterProducts(ProductSearchCriteria criteria, Pageable pageable) {
		Specification<Product> spec = ProductSpecification.fromCriteria(criteria);
		Pageable adjustedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				getSortOrder(criteria.getSort()));

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
		return convertToDto(product);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO getProductBySlug(String slug) {
		Product product = productRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
		return convertToDto(product);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO getProductById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		return convertToDto(product);
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
	public ProductDTO createProduct(ProductDTO productDTO, List<ProductImageDTO> images, Integer shopId) {
		Product product = modelMapper.map(productDTO, Product.class);

		product.setSlug(slugify.slugify(productDTO.getProductName()));

		product.setShop(
				shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));
		product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found")));

		Product savedProduct = productRepository.save(product);

		if (images != null && !images.isEmpty()) {
			images.forEach(imageDTO -> {
				ProductImage productImage = modelMapper.map(imageDTO, ProductImage.class);
				productImage.setProduct(savedProduct);
				productImageRepository.save(productImage);
			});
		}

		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	@Transactional
	public ProductDTO updateProduct(Integer id, ProductDTO productDTO, List<ProductImageDTO> imageDTOs,
			Integer shopId) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		boolean originalIsActive = existingProduct.isActive();

		modelMapper.map(productDTO, existingProduct);
		existingProduct.setActive(originalIsActive);
		existingProduct.setId(id);
		existingProduct.setSlug(slugify.slugify(productDTO.getProductName()));
		existingProduct.setShop(
				shopRepository.findById(shopId).orElseThrow(() -> new ResourceNotFoundException("Shop not found")));
		existingProduct.setCategory(categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found")));

		if (productDTO.getBrandId() != null) {
			existingProduct.setBrand(brandRepository.findById(productDTO.getBrandId()).orElseThrow(
					() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId())));
		} else {
			existingProduct.setBrand(null);
		}

		updateProductImages(existingProduct, imageDTOs);

		Product updatedProduct = productRepository.save(existingProduct);
		return convertToDto(updatedProduct);
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
	public List<ProductDTO> getProductsByShopId(Integer shopId) {
		return productRepository.findAllByShop_Id(shopId).stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void updateProductStock(Integer productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

		int totalStock = product.getVariants().stream().mapToInt(ProductVariant::getStockQuantity).sum();

		product.setStockQuantity(totalStock);

		productRepository.save(product);
	}

	private void updateProductImages(Product product, List<ProductImageDTO> imageDTOs) {
		Map<Integer, ProductImage> existingImagesMap = product.getImages().stream()
				.collect(Collectors.toMap(ProductImage::getId, Function.identity()));

		List<ProductImageDTO> imagesToProcess = imageDTOs.stream().filter(
				dto -> (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) || dto.getId() != null)
				.collect(Collectors.toList());

		for (ProductImageDTO dto : imagesToProcess) {
			ProductImage image = existingImagesMap.get(dto.getId());
			if (image != null) {
				modelMapper.map(dto, image);
				existingImagesMap.remove(dto.getId());
			} else {
				image = modelMapper.map(dto, ProductImage.class);
				image.setProduct(product);
				product.getImages().add(image);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getAllProducts() {
		return productRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void toggleProductActiveStatus(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		product.setActive(!product.isActive());
		productRepository.save(product);
	}
}
