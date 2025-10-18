package com.example.demo.repository;

import com.example.demo.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    long countByProductId(Integer productId);
    
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.product p LEFT JOIN FETCH p.images WHERE w.user.userId = :userId")
    List<Wishlist> findByUserUserId(@Param("userId") Integer userId);
    
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.user.userId = :userId AND w.product.id = :productId")
    boolean existsByUserUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.userId = :userId AND w.product.id = :productId")
    void deleteByUserUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

}
