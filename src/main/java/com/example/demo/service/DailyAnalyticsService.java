package com.example.demo.service;

import com.example.demo.entity.Order;

public interface DailyAnalyticsService {
    
    /**
     * Update daily analytics when an order is completed
     */
    void updateDailyAnalyticsForOrder(Order order);
    
    /**
     * Recalculate analytics for a specific date (for data correction)
     */
    void recalculateDailyAnalytics(Integer shopId, java.time.LocalDate date);
}
