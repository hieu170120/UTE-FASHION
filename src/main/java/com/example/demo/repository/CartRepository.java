package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    @Query("SELECT DISTINCT c FROM Cart c "
            + "LEFT JOIN FETCH c.cartItems items "
            + "LEFT JOIN FETCH items.product p "
            + "LEFT JOIN FETCH p.images "
            + "LEFT JOIN FETCH p.brand "
            + "LEFT JOIN FETCH items.variant v "
            + "LEFT JOIN FETCH v.size "
            + "LEFT JOIN FETCH v.color "
            + "WHERE c.user.userId = :userId")
    Optional<Cart> findByUserUserId(@Param("userId") Integer userId);
    
    @Query("SELECT DISTINCT c FROM Cart c "
            + "LEFT JOIN FETCH c.cartItems items "
            + "LEFT JOIN FETCH items.product p "
            + "LEFT JOIN FETCH p.images "
            + "LEFT JOIN FETCH p.brand "
            + "LEFT JOIN FETCH items.variant v "
            + "LEFT JOIN FETCH v.size "
            + "LEFT JOIN FETCH v.color "
            + "WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionId(@Param("sessionId") String sessionId);
}
