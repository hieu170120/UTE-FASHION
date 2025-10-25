package com.example.demo.service.impl;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO getCartByUserId(Integer userId) {
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseGet(() -> createNewCart(userId, null));
        return mapToCartDTO(cart);
    }

    @Override
    public CartDTO getCartBySessionId(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewCart(null, sessionId));
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO addToCart(CartItemDTO cartItemDTO, Integer userId, String sessionId) {
        Cart cart;
        if (userId != null) {
            cart = cartRepository.findByUserUserId(userId)
                    .orElseGet(() -> createNewCart(userId, null));
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createNewCart(null, sessionId));
        }

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItemDTO.getProductId()));

        // Kiểm tra xem sản phẩm với variant này đã có trong giỏ chưa
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemDTO.getProductId()))
                .filter(item -> {
                    // Nếu cả hai đều không có variant (sản phẩm đơn giản)
                    if (cartItemDTO.getVariantId() == null && item.getVariant() == null) {
                        return true;
                    }
                    // Nếu cả hai đều có variant và variant giống nhau
                    if (cartItemDTO.getVariantId() != null && item.getVariant() != null) {
                        return item.getVariant().getId().equals(cartItemDTO.getVariantId());
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Nếu đã có, tăng số lượng
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDTO.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // Nếu chưa có, tạo mới
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            
            // Sử dụng giá sale nếu có, không thì dùng giá gốc
            BigDecimal basePrice = (product.getSalePrice() != null && product.getSalePrice().compareTo(product.getPrice()) < 0)
                    ? product.getSalePrice()
                    : product.getPrice();
            cartItem.setPrice(basePrice);

            if (cartItemDTO.getVariantId() != null) {
                ProductVariant variant = variantRepository.findById(cartItemDTO.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + cartItemDTO.getVariantId()));
                cartItem.setVariant(variant);
                cartItem.setPrice(variant.getPriceAdjustment().add(basePrice));
            }

            cart.getCartItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(Integer cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        // Chỉ trả CartDTO rỗng (batch update không cần dữ liệu chi tiết)
        return new CartDTO();
    }

    @Override
    @Transactional
    public void removeCartItem(Integer cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId, String sessionId) {
        Cart cart;
        if (userId != null) {
            cart = cartRepository.findByUserUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session: " + sessionId));
        }
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public CartDTO calculateCartTotals(CartDTO cartDTO) {
        BigDecimal total = cartDTO.getCartItems().stream()
                .map(item -> {
                    // Sử dụng giá sale nếu có, không thì dùng giá gốc
                    BigDecimal effectivePrice = (item.getSalePrice() != null && item.getSalePrice().compareTo(item.getPrice()) < 0)
                            ? item.getSalePrice()
                            : item.getPrice();
                    return effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cartDTO.setTotalAmount(total);
        return cartDTO;
    }

    private Cart createNewCart(Integer userId, String sessionId) {
        Cart cart = new Cart();
        if (userId != null) {
            User user = new User();
            user.setUserId(userId);
            cart.setUser(user);
        }
        cart.setSessionId(sessionId);
        return cartRepository.save(cart);
    }

    private CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setCartItems(cart.getCartItems().stream()
                .map(item -> mapToCartItemDTO(item))
                .collect(Collectors.toList()));
        return calculateCartTotals(cartDTO);
    }
    
    private CartItemDTO mapToCartItemDTO(CartItem item) {
        CartItemDTO itemDTO = modelMapper.map(item, CartItemDTO.class);
        itemDTO.setProductId(item.getProduct().getId());
        itemDTO.setProductName(item.getProduct().getProductName());
        itemDTO.setProductImage(getPrimaryImageUrl(item.getProduct()));
        
        // Set sale price from product if exists
        if (item.getProduct().getSalePrice() != null) {
            itemDTO.setSalePrice(item.getProduct().getSalePrice());
        }
        
        // Tính totalPrice dựa trên giá hiện hành (giá sale nếu có)
        BigDecimal effectivePrice = (itemDTO.getSalePrice() != null && itemDTO.getSalePrice().compareTo(item.getPrice()) < 0)
                ? itemDTO.getSalePrice()
                : item.getPrice();
        itemDTO.setTotalPrice(effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        
        // Add variant info if exists
        if (item.getVariant() != null) {
            ProductVariant variant = item.getVariant();
            if (variant.getSize() != null) {
                itemDTO.setVariantSize(variant.getSize().getSizeName());
            }
            if (variant.getColor() != null) {
                itemDTO.setVariantColor(variant.getColor().getColorName());
            }
        }
        
        // Add brand info
        if (item.getProduct().getBrand() != null) {
            itemDTO.setBrandName(item.getProduct().getBrand().getBrandName());
        }
        
        return itemDTO;
    }
    
    private String getPrimaryImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return "/images/default-product.png";
        }
        return product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse("/images/default-product.png");
    }

	@Override
	public Integer getCartItemCount(Integer userId, String sessionId) {
		return null;
	}
}