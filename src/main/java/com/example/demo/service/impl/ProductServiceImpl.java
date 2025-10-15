
package com.example.demo.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.repository.*;
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
    private ShopRepository shopRepository;

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
    public Page<ProductDTO> getProductsByShop(Integer shopId, Pageable pageable) {
        return productRepository.findByShopId(shopId, pageable)
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

}
