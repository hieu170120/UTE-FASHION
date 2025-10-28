package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    
    // Top stats
    private BigDecimal totalRevenue;
    private BigDecimal revenueGrowthPercent;
    private Integer totalOrders;
    private BigDecimal ordersGrowthPercent;
    private Integer totalViews;
    private BigDecimal viewsGrowthPercent;
    
    // Order status stats
    private Integer returnedOrders;
    private BigDecimal returnedAmount;
    private Integer cancelledOrders;
    private BigDecimal cancelledAmount;
    
    // Monthly target
    private BigDecimal monthlyTarget;
    private BigDecimal currentMonthRevenue;
    private BigDecimal targetProgress; // percentage
    private BigDecimal targetGrowth; // +12% by default
    
    // Daily revenue chart (last 7 days)
    private List<DailyRevenueDTO> dailyRevenue;
    private BigDecimal maxDailyRevenue; // For chart scaling
    private Integer maxDailyOrders; // For chart scaling
    private String revenueLinePath; // SVG path for revenue line
    private String orderLinePath; // SVG path for order line
    
    // Top categories
    private List<CategorySalesDTO> topCategories;
    private BigDecimal totalCategoryRevenue;
    
    // Conversion funnel
    private ConversionFunnelDTO conversionFunnel;
    
    // Recent orders
    private List<RecentOrderDTO> recentOrders;
    
    // High stock products
    private List<HighStockProductDTO> highStockProducts;
    
    // 🆕 Commission fields
    private BigDecimal commissionPercentage; // Tỷ lệ chiết khấu (%)
    private BigDecimal commissionAmount;    // Tiền chiết khấu kỳ này
    private BigDecimal shopNetRevenue;       // Tiền shop thực nhận (sau trừ chiết khấu)
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenueDTO {
        private LocalDate date;
        private String dateLabel; // "12 Aug"
        private BigDecimal revenue;
        private Integer orderCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySalesDTO {
        private String categoryName;
        private BigDecimal revenue;
        private Integer orderCount;
        private String color;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionFunnelDTO {
        private Integer viewCount;
        private BigDecimal viewChange;
        private Integer addToCartCount;
        private BigDecimal cartChange;
        private Integer checkoutCount;
        private BigDecimal checkoutChange;
        private Integer completedCount;
        private BigDecimal completedChange;
        private Integer returnedCount;
        private BigDecimal returnedPercent;
        private Integer cancelledCount;
        private BigDecimal cancelledPercent;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderDTO {
        private String orderNumber;
        private String customerName;
        private String customerInitials;
        private String productName;
        private String productEmoji;
        private Integer quantity;
        private BigDecimal totalAmount;
        private String status;
        private String statusClass; // for CSS styling
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighStockProductDTO {
        private String productName;
        private Integer stockQuantity;
        private Integer soldQuantity;
        private BigDecimal stockValue; // Stock quantity * price
    }
}
