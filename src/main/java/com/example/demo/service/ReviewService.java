package com.example.demo.service;

import com.example.demo.dto.RatingStatisticsDTO;
import com.example.demo.dto.ReviewCreationRequest;
import com.example.demo.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Get a paginated list of approved reviews for a specific product.
     * @param productId The ID of the product.
     * @param pageable Pagination information.
     * @return A page of ReviewDTOs.
     */
    Page<ReviewDTO> getReviewsByProductId(Integer productId, Pageable pageable);

    /**
     * Creates a new review for a product.
     * @param request The review creation request containing review data.
     * @param userId The ID of the user creating the review.
     * @return The created ReviewDTO.
     */
    ReviewDTO createReview(ReviewCreationRequest request, Integer userId);
    
    /**
     * Get rating statistics for a product (breakdown by star rating).
     * @param productId The ID of the product.
     * @return Rating statistics including count and percentage for each star rating.
     */
    RatingStatisticsDTO getRatingStatistics(Integer productId);
}
