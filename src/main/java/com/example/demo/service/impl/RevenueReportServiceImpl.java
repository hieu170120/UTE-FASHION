package com.example.demo.service.impl;

import com.example.demo.dto.RevenueReportDTO;
import com.example.demo.dto.RevenueReportDTO.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.RevenueReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RevenueReportServiceImpl implements RevenueReportService {

    @Autowired
    private OrderRepository orderRepository;
    
    // Inner class to hold period range data
    private static class PeriodRange {
        LocalDate start;
        LocalDate end;
        LocalDate previousStart;
        LocalDate previousEnd;
        
        PeriodRange(LocalDate start, LocalDate end, LocalDate previousStart, LocalDate previousEnd) {
            this.start = start;
            this.end = end;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
        }
    }
    
    @Autowired
    private ShopAnalyticsRepository shopAnalyticsRepository;
    
    @Autowired
    private ConversionAnalyticsRepository conversionAnalyticsRepository;
    
    @Autowired
    private CategorySalesRepository categorySalesRepository;
    
    private static final String[] COLORS = {"#ff8c42", "#ffb366", "#ffd4a8", "#ffe8d6"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    @Override
    @Transactional(readOnly = true)
    public RevenueReportDTO getRevenueReport(Integer shopId, LocalDate startDate, LocalDate endDate) {
        RevenueReportDTO report = new RevenueReportDTO();
        
        // Calculate previous period for growth comparison
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate previousStart = startDate.minusDays(daysBetween + 1);
        LocalDate previousEnd = startDate.minusDays(1);
        
        // Top stats - current vs previous period
        BigDecimal currentRevenue = calculateRevenue(shopId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        BigDecimal previousRevenue = calculateRevenue(shopId, previousStart.atStartOfDay(), previousEnd.atTime(23, 59, 59));
        
        Integer currentOrders = countOrders(shopId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        Integer previousOrders = countOrders(shopId, previousStart.atStartOfDay(), previousEnd.atTime(23, 59, 59));
        
        Integer currentViews = getViewCount(shopId, startDate, endDate);
        Integer previousViews = getViewCount(shopId, previousStart, previousEnd);
        
        report.setTotalRevenue(currentRevenue);
        report.setRevenueGrowthPercent(calculateGrowthPercent(currentRevenue, previousRevenue));
        report.setTotalOrders(currentOrders);
        report.setOrdersGrowthPercent(calculateGrowthPercent(
                BigDecimal.valueOf(currentOrders), BigDecimal.valueOf(previousOrders)));
        report.setTotalViews(currentViews);
        report.setViewsGrowthPercent(calculateGrowthPercent(
                BigDecimal.valueOf(currentViews), BigDecimal.valueOf(previousViews)));
        
        // Monthly target (current month)
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);
        BigDecimal lastMonthRevenue = calculateRevenue(shopId, lastMonthStart.atStartOfDay(), lastMonthEnd.atTime(23, 59, 59));
        BigDecimal currentMonthRevenue = calculateRevenue(shopId, monthStart.atStartOfDay(), LocalDate.now().atTime(23, 59, 59));
        BigDecimal monthlyTarget = lastMonthRevenue.multiply(BigDecimal.valueOf(1.12));
        BigDecimal targetProgress = monthlyTarget.compareTo(BigDecimal.ZERO) > 0 
                ? currentMonthRevenue.divide(monthlyTarget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        report.setMonthlyTarget(monthlyTarget);
        report.setCurrentMonthRevenue(currentMonthRevenue);
        report.setTargetProgress(targetProgress);
        report.setTargetGrowth(BigDecimal.valueOf(12));
        
        // Daily revenue chart
        List<DailyRevenueDTO> dailyRevenue = calculateDailyRevenue(shopId, startDate, endDate);
        report.setDailyRevenue(dailyRevenue);
        
        BigDecimal maxRevenue = dailyRevenue.stream()
                .map(DailyRevenueDTO::getRevenue)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        Integer maxOrders = dailyRevenue.stream()
                .map(DailyRevenueDTO::getOrderCount)
                .max(Integer::compareTo)
                .orElse(1);
        report.setMaxDailyRevenue(maxRevenue.compareTo(BigDecimal.ZERO) > 0 ? maxRevenue : BigDecimal.ONE);
        report.setMaxDailyOrders(maxOrders > 0 ? maxOrders : 1);
        
        report.setRevenueLinePath(buildSvgPath(dailyRevenue, maxRevenue, 160, true));
        report.setOrderLinePath(buildSvgPath(dailyRevenue, BigDecimal.valueOf(maxOrders), 140, false));
        
        // Top categories
        report.setTopCategories(calculateTopCategories(shopId, startDate, endDate));
        report.setTotalCategoryRevenue(currentRevenue);
        
        // Conversion funnel
        report.setConversionFunnel(calculateConversionFunnel(shopId, startDate, endDate, currentOrders));
        
        // Recent orders (always show last 4 regardless of date range)
        report.setRecentOrders(getRecentOrders(shopId));
        
        return report;
    }
    
    private BigDecimal calculateRevenue(Integer shopId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        return shopAnalyticsRepository.findDayAnalyticsByDateRange(shopId, startDate, endDate).stream()
                .map(ShopAnalytics::getTotalRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Integer countOrders(Integer shopId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        return shopAnalyticsRepository.findDayAnalyticsByDateRange(shopId, startDate, endDate).stream()
                .mapToInt(a -> a.getTotalOrders() != null ? a.getTotalOrders() : 0)
                .sum();
    }
    
    private Integer getViewCount(Integer shopId, LocalDate start, LocalDate end) {
        return conversionAnalyticsRepository.findDayAnalyticsByDateRange(shopId, start, end).stream()
            .mapToInt(a -> a.getViewCount() != null ? a.getViewCount() : 0)
            .sum();
    }
    
    private BigDecimal calculateGrowthPercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
    
    private List<DailyRevenueDTO> calculateDailyRevenue(Integer shopId, LocalDate start, LocalDate end) {
        // Fetch all analytics once
        List<ShopAnalytics> analytics = shopAnalyticsRepository.findDayAnalyticsByDateRange(shopId, start, end);
        Map<LocalDate, ShopAnalytics> analyticsMap = analytics.stream()
            .collect(Collectors.toMap(ShopAnalytics::getPeriodStart, a -> a));
        
        List<DailyRevenueDTO> dailyData = new ArrayList<>();
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            ShopAnalytics dayAnalytics = analyticsMap.get(date);
            
            DailyRevenueDTO daily = new DailyRevenueDTO();
            daily.setDate(date);
            daily.setDateLabel(date.format(DATE_FORMATTER));
            daily.setRevenue(dayAnalytics != null && dayAnalytics.getTotalRevenue() != null 
                ? dayAnalytics.getTotalRevenue() : BigDecimal.ZERO);
            daily.setOrderCount(dayAnalytics != null && dayAnalytics.getTotalOrders() != null 
                ? dayAnalytics.getTotalOrders() : 0);
            
            dailyData.add(daily);
        }
        
        return dailyData;
    }
    
    private List<CategorySalesDTO> calculateTopCategories(Integer shopId, LocalDate start, LocalDate end) {
        Map<String, CategorySalesDTO> categoryMap = new HashMap<>();
        
        categorySalesRepository.findDaySalesByDateRange(shopId, start, end).stream()
                .filter(sales -> sales.getCategory() != null)
                .forEach(sales -> {
                    String categoryName = sales.getCategory().getCategoryName();
                    CategorySalesDTO dto = categoryMap.getOrDefault(categoryName,
                            new CategorySalesDTO(categoryName, BigDecimal.ZERO, 0, ""));
                    
                    dto.setRevenue(dto.getRevenue().add(sales.getTotalRevenue()));
                    dto.setOrderCount(dto.getOrderCount() + sales.getTotalOrders());
                    
                    categoryMap.put(categoryName, dto);
                });
        
        List<CategorySalesDTO> topCategories = categoryMap.values().stream()
                .sorted(Comparator.comparing(CategorySalesDTO::getRevenue).reversed())
                .limit(4)
                .collect(Collectors.toList());
        
        for (int i = 0; i < topCategories.size(); i++) {
            topCategories.get(i).setColor(COLORS[i % COLORS.length]);
        }
        
        return topCategories;
    }
    
    private ConversionFunnelDTO calculateConversionFunnel(Integer shopId, LocalDate periodStart, LocalDate periodEnd, Integer completedCount) {
        List<ConversionAnalytics> analyticsList = conversionAnalyticsRepository.findDayAnalyticsByDateRange(shopId, periodStart, periodEnd);
        
        ConversionFunnelDTO funnel = new ConversionFunnelDTO();
        
        if (!analyticsList.isEmpty()) {
            // Sum all DAY records in the period
            Integer totalViews = analyticsList.stream()
                .mapToInt(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
            Integer totalCart = analyticsList.stream()
                .mapToInt(a -> a.getAddToCartCount() != null ? a.getAddToCartCount() : 0)
                .sum();
            Integer totalCheckout = analyticsList.stream()
                .mapToInt(a -> a.getCheckoutCount() != null ? a.getCheckoutCount() : 0)
                .sum();
            
            funnel.setViewCount(totalViews);
            funnel.setViewChange(BigDecimal.ZERO); // TODO: calculate vs previous period
            funnel.setAddToCartCount(totalCart);
            funnel.setCartChange(BigDecimal.ZERO);
            funnel.setCheckoutCount(totalCheckout);
            funnel.setCheckoutChange(BigDecimal.ZERO);
            funnel.setCompletedCount(completedCount);
            funnel.setCompletedChange(BigDecimal.ZERO);
        } else {
            // Fallback to estimates if no tracking data exists yet
            Integer checkoutCount = (int) (completedCount * 1.35);
            Integer addToCartCount = (int) (checkoutCount * 1.93);
            Integer viewCount = (int) (addToCartCount * 3.84);
            
            funnel.setViewCount(viewCount);
            funnel.setViewChange(BigDecimal.ZERO);
            funnel.setAddToCartCount(addToCartCount);
            funnel.setCartChange(BigDecimal.ZERO);
            funnel.setCheckoutCount(checkoutCount);
            funnel.setCheckoutChange(BigDecimal.ZERO);
            funnel.setCompletedCount(completedCount);
            funnel.setCompletedChange(BigDecimal.ZERO);
        }
        
        return funnel;
    }
    
    private List<RecentOrderDTO> getRecentOrders(Integer shopId) {
        List<Order> recentOrders = orderRepository.findRecentOrdersByShopId(shopId, PageRequest.of(0, 4));
        
        List<RecentOrderDTO> dtos = new ArrayList<>();
        
        for (Order order : recentOrders) {
            RecentOrderDTO dto = new RecentOrderDTO();
            dto.setOrderNumber(order.getOrderNumber());
            
            if (order.getUser() != null) {
                dto.setCustomerName(order.getRecipientName() != null ? order.getRecipientName() : "Guest");
                dto.setCustomerInitials(getInitials(dto.getCustomerName()));
            } else {
                dto.setCustomerName("Guest");
                dto.setCustomerInitials("G");
            }
            
            // Get first product from order
            if (!order.getOrderItems().isEmpty()) {
                OrderItem firstItem = order.getOrderItems().iterator().next();
                dto.setProductName(firstItem.getProductName());
                dto.setProductEmoji(getCategoryEmoji(firstItem.getProduct()));
                dto.setQuantity(order.getOrderItems().stream()
                        .mapToInt(OrderItem::getQuantity)
                        .sum());
            } else {
                dto.setProductName("N/A");
                dto.setProductEmoji("📦");
                dto.setQuantity(0);
            }
            
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getOrderStatus());
            dto.setStatusClass(getStatusClass(order.getOrderStatus()));
            
            dtos.add(dto);
        }
        
        return dtos;
    }
    
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
    
    private String getCategoryEmoji(Product product) {
        if (product == null || product.getCategory() == null) return "📦";
        
        String categoryName = product.getCategory().getCategoryName().toLowerCase();
        if (categoryName.contains("áo") && categoryName.contains("nam")) return "👕";
        if (categoryName.contains("áo") && categoryName.contains("nữ")) return "👚";
        if (categoryName.contains("váy")) return "👗";
        if (categoryName.contains("quần")) return "👖";
        if (categoryName.contains("phụ kiện")) return "👜";
        return "👔";
    }
    
    private String getStatusClass(String status) {
        if (status == null) return "status-pending";
        
        switch (status.toLowerCase()) {
            case "delivered":
            case "hoàn thành":
                return "status-delivered";
            case "shipping":
            case "đang giao":
                return "status-shipping";
            default:
                return "status-pending";
        }
    }
    
    private String buildSvgPath(List<DailyRevenueDTO> dailyData, BigDecimal maxValue, double height, boolean isRevenue) {
        if (dailyData.isEmpty() || maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return "M 60,200";
        }
        
        StringBuilder path = new StringBuilder("M ");
        for (int i = 0; i < dailyData.size(); i++) {
            DailyRevenueDTO day = dailyData.get(i);
            int x = 60 + i * 100;
            
            BigDecimal value = isRevenue ? day.getRevenue() : BigDecimal.valueOf(day.getOrderCount());
            double y = 200 - (value.doubleValue() / maxValue.doubleValue()) * height;
            
            if (i > 0) {
                path.append(" L ");
            }
            path.append(x).append(",").append(String.format("%.2f", y));
        }
        
        return path.toString();
    }
}
