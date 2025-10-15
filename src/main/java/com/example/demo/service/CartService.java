package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;

public interface CartService {
    CartDTO getCartByUserId(Integer userId);
    CartDTO getCartBySessionId(String sessionId);
    CartDTO addToCart(CartItemDTO cartItemDTO, Integer userId, String sessionId);
    CartDTO updateCartItem(Integer cartItemId, Integer quantity);
    void removeCartItem(Integer cartItemId);
    void clearCart(Integer userId, String sessionId);
    CartDTO calculateCartTotals(CartDTO cartDTO);
	Integer getCartItemCount(Integer userId, String sessionId);
}