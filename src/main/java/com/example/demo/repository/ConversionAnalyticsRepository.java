package com.example.demo.repository;

import com.example.demo.entity.ConversionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ConversionAnalyticsRepository extends JpaRepository<ConversionAnalytics, Integer> {
    
    @Query("SELECT ca FROM ConversionAnalytics ca WHERE ca.shop.id = :shopId " +
           "AND ca.periodType = 'WEEK' " +
           "AND ca.periodStart <= :date AND ca.periodEnd >= :date")
    Optional<ConversionAnalytics> findCurrentWeekAnalytics(@Param("shopId") Integer shopId,
                                                           @Param("date") LocalDate date);
}
