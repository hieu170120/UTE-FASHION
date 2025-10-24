package com.example.demo.dto;

import com.example.demo.entity.PromotionRule;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionRuleDTO {
    private Integer id;
    private Integer promotionId;
    private PromotionRule.RuleType ruleType;
    private String ruleValue;
    private PromotionRule.Operator operator;
    private LocalDateTime createdAt;
    
    // Thông tin bổ sung cho hiển thị
    private String ruleTypeDisplayName;
    private String operatorDisplayName;
    private String ruleValueDisplay; // Tên sản phẩm, danh mục, thương hiệu
}

