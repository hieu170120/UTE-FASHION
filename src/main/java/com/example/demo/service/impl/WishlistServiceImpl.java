package com.example.demo.service.impl;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void addToWishlist(Integer userId, Integer productId) {
        // Kiểm tra xem đã tồn tại chưa
        if (wishlistRepository.existsByUserUserIdAndProductId(userId, productId)) {
            return; // Đã có rồi thì không thêm nữa
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Integer userId, Integer productId) {
        wishlistRepository.deleteByUserUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean isInWishlist(Integer userId, Integer productId) {
        return wishlistRepository.existsByUserUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Wishlist> getUserWishlist(Integer userId) {
        return wishlistRepository.findByUserUserId(userId);
    }

    @Override
    public long getWishlistCount(Integer productId) {
        return wishlistRepository.countByProductId(productId);
    }
}
