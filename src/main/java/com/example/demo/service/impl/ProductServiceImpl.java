
package com.example.demo.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.entity.ProductVariant;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BrandRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ColorRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SizeRepository;
import com.example.demo.service.ProductService;

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
	@Transactional(readOnly = true)
	public Page<ProductDTO> getAllProducts(Pageable pageable) {
		return productRepository.findAll(pageable).map(product -> modelMapper.map(product, ProductDTO.class));
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
	public ProductDTO getProductBySlug(String slug) {
		Product product = productRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductDTO> getProductsByCategory(String slug, Pageable pageable) {
		return productRepository.findByCategorySlug(slug, pageable)
				.map(product -> modelMapper.map(product, ProductDTO.class));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductDTO> getProductsByBrand(String slug, Pageable pageable) {
		return productRepository.findByBrandSlug(slug, pageable)
				.map(product -> modelMapper.map(product, ProductDTO.class));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
		return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable)
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

		product.setCategory(categoryRepository.getReferenceById(productDTO.getCategory().getId()));
		product.setBrand(brandRepository.getReferenceById(productDTO.getBrand().getId()));

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
				variant.setStockQuantity(varDTO.getStockQuantity());

				variant.setSize(sizeRepository.getReferenceById(varDTO.getSize().getId()));
				variant.setColor(colorRepository.getReferenceById(varDTO.getColor().getId()));
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
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		existingProduct.setProductName(productDTO.getProductName());
		existingProduct.setSlug(productDTO.getSlug());
		existingProduct.setDescription(productDTO.getLongDescription());
		existingProduct.setPrice(productDTO.getBasePrice());

		existingProduct.setCategory(categoryRepository.getReferenceById(productDTO.getCategory().getId()));
		existingProduct.setBrand(brandRepository.getReferenceById(productDTO.getBrand().getId()));

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
				variant.setStockQuantity(varDTO.getStockQuantity());

				variant.setSize(sizeRepository.getReferenceById(varDTO.getSize().getId()));
				variant.setColor(colorRepository.getReferenceById(varDTO.getColor().getId()));
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
