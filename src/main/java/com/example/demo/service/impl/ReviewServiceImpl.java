package com.example.demo.service.impl;

import com.example.demo.dto.ReviewCreationRequest;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByProductId(Integer productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByProduct_IdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);
        return reviewPage.map(this::convertToDto);
    }

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewCreationRequest request, Integer userId) {
        boolean isVerifiedPurchase = false;
        Order order = null;
        
        // 1. If orderId is provided, validate purchase
        if (request.getOrderId() != null) {
            boolean hasPurchased = orderRepository.hasUserPurchasedProduct(userId, request.getProductId(), "Delivered");
            if (!hasPurchased) {
                throw new AccessDeniedException("You can only review products you have purchased.");
            }
            
            // Check for duplicate review for the specific product in the specific order
            if (reviewRepository.existsByUser_UserIdAndOrder_IdAndProduct_Id(userId, request.getOrderId(), request.getProductId())) {
                throw new IllegalStateException("You have already reviewed this product for this order.");
            }
            
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            isVerifiedPurchase = true;
        } else {
            // Check if user has already reviewed this product without order
            if (reviewRepository.existsByUser_UserIdAndProduct_IdAndOrderIsNull(userId, request.getProductId())) {
                throw new IllegalStateException("You have already reviewed this product.");
            }
        }

        // 2. Fetch entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // 3. Create and save review
        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(isVerifiedPurchase)
                .isApproved(true) // Auto-approve for testing (change to false in production)
                .build();

        Review savedReview = reviewRepository.save(review);

        // 5. Handle and save images/videos
        Set<ReviewImage> reviewImages = new HashSet<>();
        if (request.getImageUrls() != null) {
            for (String imageUrl : request.getImageUrls()) {
                reviewImages.add(ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build());
            }
        }
        if (request.getVideoUrls() != null) {
            for (String videoUrl : request.getVideoUrls()) {
                reviewImages.add(ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl("")  // Set empty string to satisfy NOT NULL constraint
                        .videoUrl(videoUrl)
                        .build());
            }
        }

        if (!reviewImages.isEmpty()) {
            reviewImageRepository.saveAll(reviewImages);
            savedReview.setImages(reviewImages);
        }

        return convertToDto(savedReview);
    }

    private ReviewDTO convertToDto(Review review) {
        ReviewDTO dto = modelMapper.map(review, ReviewDTO.class);
        if (review.getUser() != null) {
            dto.setUserFullName(review.getUser().getFullName());
            dto.setUserAvatar(review.getUser().getAvatarUrl());
        }
        if (review.getImages() != null) {
            dto.setImageUrls(review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .filter(url -> url != null && !url.isEmpty())
                .collect(Collectors.toList()));
            dto.setVideoUrls(review.getImages().stream()
                .map(ReviewImage::getVideoUrl)
                .filter(url -> url != null && !url.isEmpty())
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
