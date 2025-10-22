package com.example.demo.service;

import com.example.demo.dto.RevenueReportDTO;
import java.time.LocalDate;

public interface RevenueReportService {
    
    /**
     * Get comprehensive revenue report for a shop within date range
     * @param shopId Shop ID
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return Complete revenue report with analytics
     */
    RevenueReportDTO getRevenueReport(Integer shopId, LocalDate startDate, LocalDate endDate);
}
