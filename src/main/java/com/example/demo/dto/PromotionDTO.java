package com.example.demo.dto;

import com.example.demo.entity.Promotion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionDTO {
    private Integer id;
    private String promotionName;
    private String description;
    private Promotion.PromotionType promotionType;
    private Promotion.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer usageLimitPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean isActive;
    private Integer priority;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PromotionRuleDTO> rules;
    
    // Thống kê
    private Long totalUsage;
    private BigDecimal totalDiscountAmount;
    private String status; // ACTIVE, EXPIRED, UPCOMING, INACTIVE
    private boolean isExpiringSoon; // Sắp hết hạn trong 7 ngày
    
    // Manual getters and setters
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public boolean isExpiringSoon() {
        return isExpiringSoon;
    }
    
    public void setExpiringSoon(boolean isExpiringSoon) {
        this.isExpiringSoon = isExpiringSoon;
    }
}
