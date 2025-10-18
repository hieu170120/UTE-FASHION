package com.example.demo.service;

import com.example.demo.entity.Wishlist;

import java.util.List;

public interface WishlistService {
    
    void addToWishlist(Integer userId, Integer productId);
    
    void removeFromWishlist(Integer userId, Integer productId);
    
    boolean isInWishlist(Integer userId, Integer productId);
    
    List<Wishlist> getUserWishlist(Integer userId);
    
    long getWishlistCount(Integer productId);
}
