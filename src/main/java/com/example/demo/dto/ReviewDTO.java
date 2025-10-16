package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer id;
    private Integer productId;
    private String userFullName;
    private String userAvatar;
    private int rating;
    private String title;
    private String comment;
    private boolean isVerifiedPurchase;
    private int helpfulCount;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private List<String> videoUrls;
}
