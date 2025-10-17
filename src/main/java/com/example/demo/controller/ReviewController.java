package com.example.demo.controller;

import com.example.demo.dto.ReviewCreationRequest;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.service.CloudinaryService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByProduct(
            @PathVariable Integer productId,
            @PageableDefault(size = 5, sort = "createdAt,desc") Pageable pageable) {
        Page<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewCreationRequest request,
            jakarta.servlet.http.HttpSession session) {
        try {
            System.out.println("=== REVIEW CREATE DEBUG ===");
            System.out.println("Session ID: " + session.getId());
            
            // Get user from session
            com.example.demo.entity.User currentUser = 
                (com.example.demo.entity.User) session.getAttribute("currentUser");
            
            System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() : "NULL"));
            
            if (currentUser == null) {
                System.out.println("User is null, checking all session attributes:");
                java.util.Enumeration<String> attrs = session.getAttributeNames();
                while (attrs.hasMoreElements()) {
                    String attr = attrs.nextElement();
                    System.out.println("  - " + attr + " = " + session.getAttribute(attr));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Bạn cần đăng nhập để đánh giá sản phẩm");
            }
            
            ReviewDTO createdReview = reviewService.createReview(request, currentUser.getUserId());
            return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Upload images for review
     * @param files Array of image files
     * @return List of uploaded image URLs
     */
    @PostMapping("/upload/images")
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            jakarta.servlet.http.HttpSession session) {
        com.example.demo.entity.User currentUser = 
            (com.example.demo.entity.User) session.getAttribute("currentUser");
            
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Authentication required"));
        }

        try {
            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    uploadedUrls.add(url);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("urls", uploadedUrls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Upload videos for review
     * @param files Array of video files
     * @return List of uploaded video URLs
     */
    @PostMapping("/upload/videos")
    public ResponseEntity<?> uploadVideos(
            @RequestParam("files") MultipartFile[] files,
            jakarta.servlet.http.HttpSession session) {
        com.example.demo.entity.User currentUser = 
            (com.example.demo.entity.User) session.getAttribute("currentUser");
            
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Authentication required"));
        }

        try {
            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadVideo(file);
                    uploadedUrls.add(url);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("urls", uploadedUrls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to upload videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
