package com.example.demo.controller;

import com.example.demo.dto.ReviewCreationRequest;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByProduct(
            @PathVariable Integer productId,
            @PageableDefault(size = 5, sort = "createdAt,desc") Pageable pageable) {
        Page<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewCreationRequest request) {
        // The User object from @AuthenticationPrincipal might be null if the JWT is missing or invalid
        if (user == null) {
            // You can return a more specific error message if you like
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required to post a review.");
        }
        try {
            ReviewDTO createdReview = reviewService.createReview(request, user.getId());
            return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
        } catch (Exception e) {
            // It's good practice to return a structured error response
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
