package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingStatisticsDTO {
    private long totalReviews;
    private double averageRating;
    private Map<Integer, Long> ratingCounts; // Key: rating (1-5), Value: count
    
    public long getRatingCount(int rating) {
        return ratingCounts.getOrDefault(rating, 0L);
    }
    
    public double getRatingPercentage(int rating) {
        if (totalReviews == 0) return 0.0;
        return (getRatingCount(rating) * 100.0) / totalReviews;
    }
}
