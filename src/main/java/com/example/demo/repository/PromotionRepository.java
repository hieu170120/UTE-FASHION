package com.example.demo.repository;

import com.example.demo.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    // Tìm kiếm promotions theo tên
    @Query("SELECT p FROM Promotion p WHERE p.promotionName LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Promotion> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Đếm số lượng promotion theo trạng thái
    long countByIsActive(boolean isActive);

    // Tìm kiếm promotions theo trạng thái
    Page<Promotion> findByIsActive(boolean isActive, Pageable pageable);

    // Tìm kiếm promotions theo loại
    Page<Promotion> findByPromotionType(Promotion.PromotionType promotionType, Pageable pageable);

    // Tìm kiếm promotions đang hoạt động trong khoảng thời gian
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.validFrom <= :now AND p.validTo >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    // Tìm kiếm promotions có thể áp dụng cho sản phẩm
    @Query("SELECT DISTINCT p FROM Promotion p " +
           "LEFT JOIN p.rules r " +
           "WHERE p.isActive = true " +
           "AND p.validFrom <= :now AND p.validTo >= :now " +
           "AND (p.promotionType = 'PRODUCT' OR p.promotionType = 'CATEGORY' OR p.promotionType = 'BRAND') " +
           "AND (r IS NULL OR (r.ruleType = 'PRODUCT' AND r.ruleValue = :productId) " +
           "OR (r.ruleType = 'CATEGORY' AND r.ruleValue = :categoryId) " +
           "OR (r.ruleType = 'BRAND' AND r.ruleValue = :brandId)) " +
           "ORDER BY p.priority DESC")
    List<Promotion> findApplicablePromotions(@Param("now") LocalDateTime now, 
                                           @Param("productId") String productId,
                                           @Param("categoryId") String categoryId,
                                           @Param("brandId") String brandId);

    // Thống kê tổng quan
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true")
    long countActivePromotions();

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true AND p.validFrom <= :now AND p.validTo >= :now")
    long countCurrentlyActivePromotions(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(p.usageCount) FROM Promotion p WHERE p.isActive = true")
    Long getTotalUsageCount();

    // Tìm promotion theo tên (để check duplicate)
    Optional<Promotion> findByPromotionName(String promotionName);

    // Tìm promotions sắp hết hạn (trong 7 ngày tới)
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.validTo BETWEEN :now AND :sevenDaysLater")
    List<Promotion> findPromotionsExpiringSoon(@Param("now") LocalDateTime now, 
                                             @Param("sevenDaysLater") LocalDateTime sevenDaysLater);
}

