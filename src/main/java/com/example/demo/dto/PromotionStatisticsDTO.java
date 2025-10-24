package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PromotionStatisticsDTO {
    // Thống kê tổng quan
    private Long totalPromotions;
    private Long activePromotions;
    private Long expiredPromotions;
    private Long upcomingPromotions;
    
    // Thống kê sử dụng
    private Long totalUsage;
    private BigDecimal totalDiscountAmount;
    private Long uniqueUsersUsed;
    
    // Thống kê theo thời gian
    private Map<String, Long> usageByMonth;
    private Map<String, BigDecimal> discountByMonth;
    
    // Top promotions
    private List<PromotionSummaryDTO> topPromotions;
    
    // Promotions sắp hết hạn
    private List<PromotionSummaryDTO> expiringSoon;
    
    // Thống kê theo loại
    private Map<String, Long> promotionsByType;
    private Map<String, Long> usageByType;
    
    @Data
    public static class PromotionSummaryDTO {
        private Integer id;
        private String promotionName;
        private Long usageCount;
        private BigDecimal totalDiscount;
        private LocalDateTime validTo;
    }
}
