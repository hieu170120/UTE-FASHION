package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Finds all approved reviews for a given product, ordered by creation date descending.
     */
    Page<Review> findByProduct_IdAndIsApprovedTrueOrderByCreatedAtDesc(Integer productId, Pageable pageable);

    /**
     * Checks if a user has already submitted a review for a specific product within a specific order.
     * This prevents duplicate reviews.
     * @param userId The ID of the user.
     * @param orderId The ID of the order.
     * @param productId The ID of the product.
     * @return True if a review already exists, false otherwise.
     */
    boolean existsByUser_IdAndOrder_IdAndProduct_Id(Integer userId, Integer orderId, Integer productId);

}
