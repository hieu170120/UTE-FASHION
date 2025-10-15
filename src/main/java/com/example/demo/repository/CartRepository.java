package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserUserId(Integer userId); // Sửa từ findByUserId thành findByUserUserId
    Optional<Cart> findBySessionId(String sessionId);
}