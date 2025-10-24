package com.example.demo.repository;

import com.example.demo.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Integer> {

    // Tìm usage theo promotion
    List<PromotionUsage> findByPromotionId(Integer promotionId);

    // Tìm usage theo user
    List<PromotionUsage> findByUserUserId(Integer userId);

    // Tìm usage theo order
    List<PromotionUsage> findByOrderId(Integer orderId);

    // Đếm số lần sử dụng của một promotion
    long countByPromotionId(Integer promotionId);

    // Đếm số lần sử dụng của một user cho một promotion
    long countByPromotionIdAndUserUserId(Integer promotionId, Integer userId);

    // Tổng tiền giảm giá của một promotion
    @Query("SELECT COALESCE(SUM(pu.discountAmount), 0) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
    BigDecimal getTotalDiscountAmountByPromotionId(@Param("promotionId") Integer promotionId);

    // Tổng tiền giảm giá của một user
    @Query("SELECT COALESCE(SUM(pu.discountAmount), 0) FROM PromotionUsage pu WHERE pu.user.userId = :userId")
    BigDecimal getTotalDiscountAmountByUserId(@Param("userId") Integer userId);

    // Thống kê usage theo thời gian
    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.usedAt BETWEEN :startDate AND :endDate")
    List<PromotionUsage> findUsageByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    // Top promotions được sử dụng nhiều nhất
    @Query("SELECT pu.promotion.id, COUNT(pu) as usageCount " +
           "FROM PromotionUsage pu " +
           "WHERE pu.usedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pu.promotion.id " +
           "ORDER BY usageCount DESC")
    List<Object[]> findTopPromotionsByUsage(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Kiểm tra user đã sử dụng promotion chưa
    boolean existsByPromotionIdAndUserUserId(Integer promotionId, Integer userId);

    // Tìm usage gần nhất của user
    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.user.userId = :userId ORDER BY pu.usedAt DESC")
    List<PromotionUsage> findRecentUsageByUserId(@Param("userId") Integer userId);
}
