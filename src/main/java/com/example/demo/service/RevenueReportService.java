package com.example.demo.service;

import com.example.demo.dto.RevenueReportDTO;

public interface RevenueReportService {
    
    /**
     * Get comprehensive revenue report for a shop
     * @param shopId Shop ID
     * @param chartPeriod Period for revenue chart
     * @param chartDate Specific date for chart if period is "day"
     * @param statsPeriod Period for top stats (revenue, orders, views)
     * @param categoryPeriod Period for top categories
     * @param conversionPeriod Period for conversion funnel
     * @return Complete revenue report with analytics
     */
    RevenueReportDTO getRevenueReport(Integer shopId, String chartPeriod, String chartDate, 
                                     String statsPeriod, String categoryPeriod, String conversionPeriod);
}
