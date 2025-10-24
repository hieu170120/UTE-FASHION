package com.example.demo.repository;

import com.example.demo.entity.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Integer> {

    // Tìm rules theo promotion
    List<PromotionRule> findByPromotionId(Integer promotionId);

    // Tìm rules theo loại
    List<PromotionRule> findByRuleType(PromotionRule.RuleType ruleType);

    // Tìm rules theo promotion và loại
    List<PromotionRule> findByPromotionIdAndRuleType(Integer promotionId, PromotionRule.RuleType ruleType);

    // Xóa tất cả rules của một promotion
    void deleteByPromotionId(Integer promotionId);

    // Tìm rules cho sản phẩm cụ thể
    @Query("SELECT r FROM PromotionRule r WHERE r.ruleType = 'PRODUCT' AND r.ruleValue = :productId")
    List<PromotionRule> findRulesForProduct(@Param("productId") String productId);

    // Tìm rules cho danh mục cụ thể
    @Query("SELECT r FROM PromotionRule r WHERE r.ruleType = 'CATEGORY' AND r.ruleValue = :categoryId")
    List<PromotionRule> findRulesForCategory(@Param("categoryId") String categoryId);

    // Tìm rules cho thương hiệu cụ thể
    @Query("SELECT r FROM PromotionRule r WHERE r.ruleType = 'BRAND' AND r.ruleValue = :brandId")
    List<PromotionRule> findRulesForBrand(@Param("brandId") String brandId);
}

