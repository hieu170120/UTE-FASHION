package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {


    Page<Review> findByProduct_IdAndIsApprovedTrueOrderByCreatedAtDesc(Integer productId, Pageable pageable);

    boolean existsByUser_UserIdAndOrder_IdAndProduct_Id(Integer userId, Integer orderId, Integer productId);

}
