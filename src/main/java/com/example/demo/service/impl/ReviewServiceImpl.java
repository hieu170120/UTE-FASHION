package com.example.demo.service.impl;

import com.example.demo.dto.RatingStatisticsDTO;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        Order order = null;
        boolean isVerifiedPurchase = false;
        
        // 1. Nếu có orderId, validate order đó
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
            
            // Validate order belongs to user
            if (!order.getUser().getUserId().equals(userId)) {
                throw new AccessDeniedException("Đơn hàng này không thuộc về bạn.");
            }
            
            // Validate order status
            if (!"Delivered".equalsIgnoreCase(order.getOrderStatus())) {
                throw new AccessDeniedException("Chỉ có thể đánh giá sản phẩm từ đơn hàng đã giao thành công. Trạng thái: " + order.getOrderStatus());
            }
            
            // Validate product is in order
            boolean productInOrder = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(request.getProductId()));
            if (!productInOrder) {
                throw new AccessDeniedException("Sản phẩm này không có trong đơn hàng.");
            }
            
            // Check duplicate review cho order này
            if (reviewRepository.existsByUser_UserIdAndOrder_IdAndProduct_Id(userId, request.getOrderId(), request.getProductId())) {
                throw new IllegalStateException("Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi.");
            }
            
            isVerifiedPurchase = true;
        } else {
            // 2. Nếu KHÔNG có orderId, tự động tìm đơn hàng đã giao của user với sản phẩm này
            List<Order> eligibleOrders = orderRepository.findEligibleOrdersForReview(userId, request.getProductId());
            
            if (eligibleOrders.isEmpty()) {
                throw new AccessDeniedException("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng thành công.");
            }
            
            // Lấy đơn hàng gần nhất
            order = eligibleOrders.get(0);
            isVerifiedPurchase = true;
        }

        // 5. Fetch entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // 6. Create and save review
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
    
    @Override
    @Transactional(readOnly = true)
    public RatingStatisticsDTO getRatingStatistics(Integer productId) {
        List<Review> reviews = reviewRepository.findAll().stream()
            .filter(r -> r.getProduct().getId().equals(productId) && r.isApproved())
            .collect(Collectors.toList());
        
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }
        
        long total = 0;
        double sum = 0;
        
        for (Review review : reviews) {
            int rating = review.getRating();
            ratingCounts.put(rating, ratingCounts.get(rating) + 1);
            sum += rating;
            total++;
        }
        
        double average = total > 0 ? sum / total : 0.0;
        
        return RatingStatisticsDTO.builder()
            .totalReviews(total)
            .averageRating(average)
            .ratingCounts(ratingCounts)
            .build();
    }
}
