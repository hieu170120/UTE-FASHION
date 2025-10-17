package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {


    @Query("SELECT DISTINCT r FROM Review r " +
           "LEFT JOIN FETCH r.images " +
           "LEFT JOIN FETCH r.user " +
           "WHERE r.product.id = :productId AND r.isApproved = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByProduct_IdAndIsApprovedTrueOrderByCreatedAtDesc(@Param("productId") Integer productId, Pageable pageable);

    boolean existsByUser_UserIdAndOrder_IdAndProduct_Id(Integer userId, Integer orderId, Integer productId);
    
    boolean existsByUser_UserIdAndProduct_IdAndOrderIsNull(Integer userId, Integer productId);

}
