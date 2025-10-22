package com.example.demo.repository;

import com.example.demo.entity.ShopAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopAnalyticsRepository extends JpaRepository<ShopAnalytics, Integer> {
    
    List<ShopAnalytics> findByShopIdAndPeriodType(Integer shopId, String periodType);
    
    Optional<ShopAnalytics> findByShopIdAndPeriodTypeAndPeriodStartAndPeriodEnd(
            Integer shopId, String periodType, LocalDate periodStart, LocalDate periodEnd);
    
    @Query("SELECT sa FROM ShopAnalytics sa WHERE sa.shop.id = :shopId " +
           "AND sa.periodType = :periodType " +
           "AND sa.periodStart >= :startDate " +
           "ORDER BY sa.periodStart DESC")
    List<ShopAnalytics> findRecentAnalytics(@Param("shopId") Integer shopId,
                                           @Param("periodType") String periodType,
                                           @Param("startDate") LocalDate startDate);
}
