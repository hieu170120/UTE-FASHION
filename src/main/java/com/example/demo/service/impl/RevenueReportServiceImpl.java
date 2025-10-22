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
    public RevenueReportDTO getRevenueReport(Integer shopId, String chartPeriod, String chartDate,
                                             String statsPeriod, String categoryPeriod, String conversionPeriod) {
        RevenueReportDTO report = new RevenueReportDTO();
        
        LocalDate today = LocalDate.now();
        LocalDate periodStart;
        LocalDate previousPeriodStart;
        LocalDate previousPeriodEnd;
        
        // Calculate period range for TOP STATS based on statsPeriod filter
        switch (statsPeriod.toLowerCase()) {
            case "day":
                // Stats always use today, not custom date
                periodStart = today;
                previousPeriodStart = today.minusDays(1);
                previousPeriodEnd = today.minusDays(1);
                break;
            case "month":
                periodStart = today.withDayOfMonth(1);
                previousPeriodStart = periodStart.minusMonths(1);
                previousPeriodEnd = periodStart.minusDays(1);
                break;
            case "year":
                periodStart = today.withDayOfYear(1);
                previousPeriodStart = periodStart.minusYears(1);
                previousPeriodEnd = periodStart.minusDays(1);
                break;
            case "week":
            default:
                periodStart = today.minusDays(6); // Last 7 days including today
                previousPeriodStart = today.minusDays(13);
                previousPeriodEnd = today.minusDays(7);
                break;
        }
        
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);
        
        // Calculate top stats for selected period
        BigDecimal currentPeriodRevenue = calculateRevenue(shopId, periodStart.atStartOfDay(), today.atTime(23, 59, 59));
        BigDecimal previousPeriodRevenue = calculateRevenue(shopId, previousPeriodStart.atStartOfDay(), previousPeriodEnd.atTime(23, 59, 59));
        
        Integer currentPeriodOrders = countOrders(shopId, periodStart.atStartOfDay(), today.atTime(23, 59, 59));
        Integer previousPeriodOrders = countOrders(shopId, previousPeriodStart.atStartOfDay(), previousPeriodEnd.atTime(23, 59, 59));
        
        report.setTotalRevenue(currentPeriodRevenue);
        report.setRevenueGrowthPercent(calculateGrowthPercent(currentPeriodRevenue, previousPeriodRevenue));
        report.setTotalOrders(currentPeriodOrders);
        report.setOrdersGrowthPercent(calculateGrowthPercent(
                BigDecimal.valueOf(currentPeriodOrders), 
                BigDecimal.valueOf(previousPeriodOrders)));
        
        // Get real view counts from ConversionAnalytics
        Integer currentPeriodViews = getViewCount(shopId, periodStart, today);
        Integer previousPeriodViews = getViewCount(shopId, previousPeriodStart, previousPeriodEnd);
        report.setTotalViews(currentPeriodViews);
        report.setViewsGrowthPercent(calculateGrowthPercent(
                BigDecimal.valueOf(currentPeriodViews),
                BigDecimal.valueOf(previousPeriodViews)));
        
        // Calculate monthly target (last month + 12%)
        BigDecimal lastMonthRevenue = calculateRevenue(shopId, lastMonthStart.atStartOfDay(), lastMonthEnd.atTime(23, 59, 59));
        BigDecimal currentMonthRevenue = calculateRevenue(shopId, monthStart.atStartOfDay(), today.atTime(23, 59, 59));
        BigDecimal monthlyTarget = lastMonthRevenue.multiply(BigDecimal.valueOf(1.12)); // +12%
        BigDecimal targetProgress = monthlyTarget.compareTo(BigDecimal.ZERO) > 0 
                ? currentMonthRevenue.divide(monthlyTarget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        report.setMonthlyTarget(monthlyTarget);
        report.setCurrentMonthRevenue(currentMonthRevenue);
        report.setTargetProgress(targetProgress);
        report.setTargetGrowth(BigDecimal.valueOf(12));
        
        // Daily revenue chart - use chartPeriod
        LocalDate chartToday = today;
        LocalDate chartStart = periodStart; // Will recalculate below based on chartPeriod
        
        switch (chartPeriod.toLowerCase()) {
            case "day":
                LocalDate selectedDate = (chartDate != null && !chartDate.isEmpty()) 
                        ? LocalDate.parse(chartDate) : today;
                chartStart = selectedDate;
                chartToday = selectedDate;
                break;
            case "month":
                chartStart = today.withDayOfMonth(1);
                break;
            case "year":
                chartStart = today.withDayOfYear(1);
                break;
            case "week":
            default:
                chartStart = today.minusDays(6);
                break;
        }
        
        List<DailyRevenueDTO> dailyRevenue = calculateDailyRevenue(shopId, chartStart, chartToday);
        report.setDailyRevenue(dailyRevenue);
        
        // Calculate max values for chart scaling
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
        
        // Build SVG paths
        report.setRevenueLinePath(buildSvgPath(dailyRevenue, maxRevenue, 160, true));
        report.setOrderLinePath(buildSvgPath(dailyRevenue, BigDecimal.valueOf(maxOrders), 140, false));
        
        // Top categories - use categoryPeriod
        LocalDate categoryStart = calculatePeriodStart(categoryPeriod, LocalDate.now());
        BigDecimal categoryRevenue = calculateRevenue(shopId, categoryStart.atStartOfDay(), LocalDate.now().atTime(23, 59, 59));
        report.setTopCategories(calculateTopCategories(shopId, categoryStart, LocalDate.now()));
        report.setTotalCategoryRevenue(categoryRevenue);
        
        // Conversion funnel - use conversionPeriod
        LocalDate conversionStart = calculatePeriodStart(conversionPeriod, LocalDate.now());
        Integer conversionOrders = countOrders(shopId, conversionStart.atStartOfDay(), LocalDate.now().atTime(23, 59, 59));
        report.setConversionFunnel(calculateConversionFunnel(shopId, conversionStart, LocalDate.now(), conversionOrders));
        
        // Recent orders
        report.setRecentOrders(getRecentOrders(shopId));
        
        return report;
    }
    
    private BigDecimal calculateRevenue(Integer shopId, LocalDateTime start, LocalDateTime end) {
        // Read from ShopAnalytics DAY records for better performance
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        return shopAnalyticsRepository.findAll().stream()
                .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
                .filter(a -> "DAY".equals(a.getPeriodType()))
                .filter(a -> a.getPeriodStart() != null)
                .filter(a -> !a.getPeriodStart().isBefore(startDate) && !a.getPeriodStart().isAfter(endDate))
                .map(ShopAnalytics::getTotalRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Integer countOrders(Integer shopId, LocalDateTime start, LocalDateTime end) {
        // Read from ShopAnalytics DAY records
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        return shopAnalyticsRepository.findAll().stream()
                .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
                .filter(a -> "DAY".equals(a.getPeriodType()))
                .filter(a -> a.getPeriodStart() != null)
                .filter(a -> !a.getPeriodStart().isBefore(startDate) && !a.getPeriodStart().isAfter(endDate))
                .mapToInt(a -> a.getTotalOrders() != null ? a.getTotalOrders() : 0)
                .sum();
    }
    
    private Integer getViewCount(Integer shopId, LocalDate start, LocalDate end) {
        // Sum view counts from ConversionAnalytics DAY records
        return conversionAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> a.getPeriodStart() != null)
            .filter(a -> !a.getPeriodStart().isBefore(start) && !a.getPeriodStart().isAfter(end))
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
        List<DailyRevenueDTO> dailyData = new ArrayList<>();
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            BigDecimal revenue = calculateRevenue(shopId, dayStart, dayEnd);
            Integer orders = countOrders(shopId, dayStart, dayEnd);
            
            DailyRevenueDTO daily = new DailyRevenueDTO();
            daily.setDate(date);
            daily.setDateLabel(date.format(DATE_FORMATTER));
            daily.setRevenue(revenue);
            daily.setOrderCount(orders);
            
            dailyData.add(daily);
        }
        
        return dailyData;
    }
    
    private List<CategorySalesDTO> calculateTopCategories(Integer shopId, LocalDate start, LocalDate end) {
        // Read from CategorySales DAY records and aggregate by category
        Map<String, CategorySalesDTO> categoryMap = new HashMap<>();
        
        categorySalesRepository.findAll().stream()
                .filter(s -> s.getShop() != null && s.getShop().getId().equals(shopId))
                .filter(s -> "DAY".equals(s.getPeriodType()))
                .filter(s -> s.getPeriodStart() != null)
                .filter(s -> !s.getPeriodStart().isBefore(start) && !s.getPeriodStart().isAfter(end))
                .forEach(sales -> {
                    if (sales.getCategory() != null) {
                        String categoryName = sales.getCategory().getCategoryName();
                        CategorySalesDTO dto = categoryMap.getOrDefault(categoryName,
                                new CategorySalesDTO(categoryName, BigDecimal.ZERO, 0, ""));
                        
                        dto.setRevenue(dto.getRevenue().add(sales.getTotalRevenue()));
                        dto.setOrderCount(dto.getOrderCount() + sales.getTotalOrders());
                        
                        categoryMap.put(categoryName, dto);
                    }
                });
        
        // Sort by revenue and take top 4
        List<CategorySalesDTO> topCategories = categoryMap.values().stream()
                .sorted(Comparator.comparing(CategorySalesDTO::getRevenue).reversed())
                .limit(4)
                .collect(Collectors.toList());
        
        // Assign colors
        for (int i = 0; i < topCategories.size(); i++) {
            topCategories.get(i).setColor(COLORS[i % COLORS.length]);
        }
        
        return topCategories;
    }
    
    private ConversionFunnelDTO calculateConversionFunnel(Integer shopId, LocalDate periodStart, LocalDate periodEnd, Integer completedCount) {
        // Aggregate ConversionAnalytics DAY records within the date range
        List<ConversionAnalytics> analyticsList = conversionAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> a.getPeriodStart() != null)
            .filter(a -> !a.getPeriodStart().isBefore(periodStart) && !a.getPeriodStart().isAfter(periodEnd))
            .collect(Collectors.toList());
        
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
        List<Order> recentOrders = orderRepository.findAll().stream()
                .filter(o -> o.getShop() != null && o.getShop().getId().equals(shopId))
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .collect(Collectors.toList());
        
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
    
    private LocalDate calculatePeriodStart(String period, LocalDate today) {
        switch (period.toLowerCase()) {
            case "day":
                return today;
            case "month":
                return today.withDayOfMonth(1);
            case "year":
                return today.withDayOfYear(1);
            case "week":
            default:
                return today.minusDays(6);
        }
    }
    
    private String buildSvgPath(List<DailyRevenueDTO> dailyData, BigDecimal maxValue, int height, boolean isRevenue) {
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
