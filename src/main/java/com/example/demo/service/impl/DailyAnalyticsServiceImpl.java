package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.DailyAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyAnalyticsServiceImpl implements DailyAnalyticsService {
	
	private static final Logger logger = LoggerFactory.getLogger(DailyAnalyticsServiceImpl.class);

    @Autowired
    private ShopAnalyticsRepository shopAnalyticsRepository;
    
    @Autowired
    private CategorySalesRepository categorySalesRepository;
    
    @Autowired
    private ConversionAnalyticsRepository conversionAnalyticsRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public void updateDailyAnalyticsForOrder(Order order) {
        if (order == null || order.getShop() == null || order.getOrderDate() == null) {
            return;
        }
        
        // Only update for completed orders
        if (!"Delivered".equalsIgnoreCase(order.getOrderStatus()) && 
            !"Hoàn thành".equalsIgnoreCase(order.getOrderStatus())) {
            return;
        }
        
        LocalDate orderDate = order.getOrderDate().toLocalDate();
        Integer shopId = order.getShop().getId();
        
        // Update ShopAnalytics
        updateShopAnalytics(shopId, orderDate, order);
        
        // Update CategorySales for each category in order
        updateCategorySales(shopId, orderDate, order);
        
        // Update ConversionAnalytics completed count
        updateConversionCompletedCount(shopId, orderDate);
    }

    @Override
    @Transactional
    public void recalculateDailyAnalytics(Integer shopId, LocalDate date) {
        // Get all completed orders for shop on this date
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> o.getShop() != null && o.getShop().getId().equals(shopId))
            .filter(o -> o.getOrderDate() != null)
            .filter(o -> o.getOrderDate().toLocalDate().equals(date))
            .filter(o -> "Delivered".equalsIgnoreCase(o.getOrderStatus()) || 
                        "Hoàn thành".equalsIgnoreCase(o.getOrderStatus()))
            .collect(Collectors.toList());
        
        // Recalculate ShopAnalytics
        recalculateShopAnalytics(shopId, date, orders);
        
        // Recalculate CategorySales
        recalculateCategorySales(shopId, date, orders);
        
        // Update completed count in ConversionAnalytics
        updateConversionCompletedCount(shopId, date);
    }

    private void updateShopAnalytics(Integer shopId, LocalDate date, Order order) {
        logger.info("🔵 [DailyAnalytics] updateShopAnalytics - START");
        logger.info("   shopId: {}, orderDate: {}, orderId: {}", shopId, date, order.getId());
        logger.info("   totalAmount: {}", order.getTotalAmount());
        
        ShopAnalytics analytics = shopAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> date.equals(a.getPeriodStart()))
            .findFirst()
            .orElseGet(() -> {
                logger.info("📍 [DailyAnalytics] Creating new ShopAnalytics record for date: {}", date);
                ShopAnalytics newAnalytics = new ShopAnalytics();
                newAnalytics.setShop(order.getShop());
                newAnalytics.setPeriodType("DAY");
                newAnalytics.setPeriodStart(date);
                newAnalytics.setPeriodEnd(date);
                newAnalytics.setTotalRevenue(BigDecimal.ZERO);
                newAnalytics.setTotalOrders(0);
                newAnalytics.setTotalViews(0);
                newAnalytics.setCommissionPercentage(BigDecimal.ZERO);
                newAnalytics.setCommissionAmount(BigDecimal.ZERO);
                newAnalytics.setShopNetRevenue(BigDecimal.ZERO);
                return newAnalytics;
            });
        
        // Add order revenue and increment order count
        logger.info("📍 [DailyAnalytics] Adding order revenue and order count");
        analytics.setTotalRevenue(analytics.getTotalRevenue().add(order.getTotalAmount()));
        analytics.setTotalOrders(analytics.getTotalOrders() + 1);
        
        // 🆕 TÍNH CHIẾT KHẤU
        Shop shop = order.getShop();
        if (shop != null && shop.getCommissionPercentage() != null) {
            BigDecimal commissionPercentage = shop.getCommissionPercentage();
            logger.info("📍 [DailyAnalytics] Calculating commission");
            logger.info("   Commission %: {}", commissionPercentage);
            
            // Tính tiền chiết khấu cho đơn hàng này
            BigDecimal commissionAmount = order.getTotalAmount()
                .multiply(commissionPercentage)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            
            // Tính tiền shop thực nhận từ đơn hàng này
            BigDecimal shopNetRevenue = order.getTotalAmount()
                .subtract(commissionAmount);
            
            logger.info("✅ [DailyAnalytics] Commission calculated");
            logger.info("   Total: {} → Commission: {} → Shop net: {}", 
                       order.getTotalAmount(), commissionAmount, shopNetRevenue);
            
            // Cộng dồn vào ShopAnalytics
            analytics.setCommissionPercentage(commissionPercentage);
            analytics.setCommissionAmount(
                (analytics.getCommissionAmount() != null ? 
                 analytics.getCommissionAmount() : BigDecimal.ZERO)
                .add(commissionAmount)
            );
            analytics.setShopNetRevenue(
                (analytics.getShopNetRevenue() != null ? 
                 analytics.getShopNetRevenue() : BigDecimal.ZERO)
                .add(shopNetRevenue)
            );
            
            logger.info("✅ [DailyAnalytics] ShopAnalytics updated");
            logger.info("   Cumulative commission: {}", analytics.getCommissionAmount());
            logger.info("   Cumulative shop net revenue: {}", analytics.getShopNetRevenue());
        } else {
            logger.warn("⚠️ [DailyAnalytics] Shop or commission percentage is NULL");
        }
        
        logger.info("📍 [DailyAnalytics] Saving ShopAnalytics");
        shopAnalyticsRepository.save(analytics);
        logger.info("✅ [DailyAnalytics] updateShopAnalytics - SUCCESS");
    }

    private void updateCategorySales(Integer shopId, LocalDate date, Order order) {
        // Group order items by category
        Map<Category, BigDecimal> categoryRevenue = new HashMap<>();
        
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() != null && item.getProduct().getCategory() != null) {
                Category category = item.getProduct().getCategory();
                BigDecimal revenue = categoryRevenue.getOrDefault(category, BigDecimal.ZERO);
                categoryRevenue.put(category, revenue.add(item.getTotalPrice()));
            }
        }
        
        // Update or create CategorySales for each category
        for (Map.Entry<Category, BigDecimal> entry : categoryRevenue.entrySet()) {
            Category category = entry.getKey();
            BigDecimal revenue = entry.getValue();
            
            CategorySales sales = categorySalesRepository.findAll().stream()
                .filter(s -> s.getShop() != null && s.getShop().getId().equals(shopId))
                .filter(s -> s.getCategory() != null && s.getCategory().getId().equals(category.getId()))
                .filter(s -> "DAY".equals(s.getPeriodType()))
                .filter(s -> date.equals(s.getPeriodStart()))
                .findFirst()
                .orElseGet(() -> {
                    CategorySales newSales = new CategorySales();
                    newSales.setShop(order.getShop());
                    newSales.setCategory(category);
                    newSales.setPeriodType("DAY");
                    newSales.setPeriodStart(date);
                    newSales.setPeriodEnd(date);
                    newSales.setTotalRevenue(BigDecimal.ZERO);
                    newSales.setTotalOrders(0);
                    return newSales;
                });
            
            sales.setTotalRevenue(sales.getTotalRevenue().add(revenue));
            sales.setTotalOrders(sales.getTotalOrders() + 1);
            
            categorySalesRepository.save(sales);
        }
    }

    private void updateConversionCompletedCount(Integer shopId, LocalDate date) {
        // Count completed orders for the day
        long completedCount = orderRepository.findAll().stream()
            .filter(o -> o.getShop() != null && o.getShop().getId().equals(shopId))
            .filter(o -> o.getOrderDate() != null)
            .filter(o -> o.getOrderDate().toLocalDate().equals(date))
            .filter(o -> "Delivered".equalsIgnoreCase(o.getOrderStatus()) || 
                        "Hoàn thành".equalsIgnoreCase(o.getOrderStatus()))
            .count();
        
        ConversionAnalytics analytics = conversionAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> date.equals(a.getPeriodStart()))
            .findFirst()
            .orElse(null);
        
        if (analytics != null) {
            analytics.setCompletedCount((int) completedCount);
            conversionAnalyticsRepository.save(analytics);
        }
    }

    private void recalculateShopAnalytics(Integer shopId, LocalDate date, List<Order> orders) {
        BigDecimal totalRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        ShopAnalytics analytics = shopAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> date.equals(a.getPeriodStart()))
            .findFirst()
            .orElseGet(() -> {
                ShopAnalytics newAnalytics = new ShopAnalytics();
                Shop shop = new Shop();
                shop.setId(shopId);
                newAnalytics.setShop(shop);
                newAnalytics.setPeriodType("DAY");
                newAnalytics.setPeriodStart(date);
                newAnalytics.setPeriodEnd(date);
                newAnalytics.setTotalViews(0);
                return newAnalytics;
            });
        
        analytics.setTotalRevenue(totalRevenue);
        analytics.setTotalOrders(orders.size());
        
        shopAnalyticsRepository.save(analytics);
    }

    private void recalculateCategorySales(Integer shopId, LocalDate date, List<Order> orders) {
        // Delete existing CategorySales for this date
        List<CategorySales> existingSales = categorySalesRepository.findAll().stream()
            .filter(s -> s.getShop() != null && s.getShop().getId().equals(shopId))
            .filter(s -> "DAY".equals(s.getPeriodType()))
            .filter(s -> date.equals(s.getPeriodStart()))
            .collect(Collectors.toList());
        
        categorySalesRepository.deleteAll(existingSales);
        
        // Recalculate from orders
        Map<Category, CategoryData> categoryMap = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null && item.getProduct().getCategory() != null) {
                    Category category = item.getProduct().getCategory();
                    CategoryData data = categoryMap.getOrDefault(category, new CategoryData());
                    data.revenue = data.revenue.add(item.getTotalPrice());
                    data.orderCount++;
                    categoryMap.put(category, data);
                }
            }
        }
        
        // Save new CategorySales
        Shop shop = new Shop();
        shop.setId(shopId);
        
        for (Map.Entry<Category, CategoryData> entry : categoryMap.entrySet()) {
            CategorySales sales = new CategorySales();
            sales.setShop(shop);
            sales.setCategory(entry.getKey());
            sales.setPeriodType("DAY");
            sales.setPeriodStart(date);
            sales.setPeriodEnd(date);
            sales.setTotalRevenue(entry.getValue().revenue);
            sales.setTotalOrders(entry.getValue().orderCount);
            
            categorySalesRepository.save(sales);
        }
    }
    
    // Helper class for category aggregation
    private static class CategoryData {
        BigDecimal revenue = BigDecimal.ZERO;
        int orderCount = 0;
    }
}
