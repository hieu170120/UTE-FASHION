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
        logger.info("🔵 STEP 1 [DailyAnalytics] updateDailyAnalyticsForOrder called");
        
        if (order == null || order.getShop() == null || order.getOrderDate() == null) {
            logger.warn("🔴 STEP 1 FAIL: order={}, shop={}, orderDate={}", 
                order, order != null ? order.getShop() : null, order != null ? order.getOrderDate() : null);
            return;
        }
        logger.info("✅ STEP 1: order, shop, orderDate all NOT NULL");
        
        // Only update for completed orders - check against OrderStatus enum
        String orderStatus = order.getOrderStatus();
        logger.info("🔵 STEP 2: Checking orderStatus = {}", orderStatus);
        
        boolean isDelivered = orderStatus != null && 
            (orderStatus.equalsIgnoreCase("Delivered") || 
             orderStatus.equalsIgnoreCase("Hoàn thành"));
        
        logger.info("   isDelivered = {}", isDelivered);
        
        if (!isDelivered) {
            logger.warn("🔴 STEP 2 FAIL: Order status is NOT DELIVERED: {}", orderStatus);
            return;
        }
        logger.info("✅ STEP 2: Order status IS DELIVERED");
        
        LocalDate orderDate = order.getOrderDate().toLocalDate();
        Integer shopId = order.getShop().getId();
        logger.info("🔵 STEP 3: orderDate={}, shopId={}", orderDate, shopId);
        
        // Update ShopAnalytics
        logger.info("🔵 STEP 4: Calling updateShopAnalytics...");
        updateShopAnalytics(shopId, orderDate, order);
        logger.info("✅ STEP 4: updateShopAnalytics completed");
        
        // Update CategorySales for each category in order
        logger.info("🔵 STEP 5: Calling updateCategorySales...");
        updateCategorySales(shopId, orderDate, order);
        logger.info("✅ STEP 5: updateCategorySales completed");
        
        // Update ConversionAnalytics completed count
        logger.info("🔵 STEP 6: Calling updateConversionCompletedCount...");
        updateConversionCompletedCount(shopId, orderDate);
        logger.info("✅ STEP 6: updateConversionCompletedCount completed");
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ [DailyAnalytics] updateDailyAnalyticsForOrder COMPLETED");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    @Override
    @Transactional
    public void recalculateDailyAnalytics(Integer shopId, LocalDate date) {
        // Get all completed orders for shop on this date
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> o.getShop() != null && o.getShop().getId().equals(shopId))
            .filter(o -> o.getOrderDate() != null)
            .filter(o -> o.getOrderDate().toLocalDate().equals(date))
            .filter(o -> {
                String status = o.getOrderStatus();
                return status != null && 
                    (status.equalsIgnoreCase("Delivered") || 
                     status.equalsIgnoreCase("Hoàn thành"));
            })
            .collect(Collectors.toList());
        
        // Recalculate ShopAnalytics
        recalculateShopAnalytics(shopId, date, orders);
        
        // Recalculate CategorySales
        recalculateCategorySales(shopId, date, orders);
        
        // Update completed count in ConversionAnalytics
        updateConversionCompletedCount(shopId, date);
    }

    /**
     * 🔥 HỎA CHIẾT KHẤU KHI ĐƠN HÀNG BỊ HOÀN TRẢ/HỦY
     * Loại bỏ chiết khấu từ ShopAnalytics khi order status chuyển sang RETURNED/REFUNDED
     */
    @Override
    @Transactional
    public void refundCommissionForOrder(Order order) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔴 [REFUND] ORDER RETURNED - Commission Refund Started");
        logger.info("   OrderID: {}, Shop: {}, TotalAmount: {}", 
            order.getId(),
            order.getShop() != null ? order.getShop().getShopName() : "Unknown",
            order.getTotalAmount());
        
        if (order == null || order.getShop() == null) {
            logger.warn("⚠️ [REFUND] Order or Shop is NULL - skipping refund");
            return;
        }
        
        LocalDate orderDate = order.getOrderDate().toLocalDate();
        Integer shopId = order.getShop().getId();
        
        try {
            ShopAnalytics analytics = shopAnalyticsRepository.findAll().stream()
                .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
                .filter(a -> "DAY".equals(a.getPeriodType()))
                .filter(a -> orderDate.equals(a.getPeriodStart()))
                .findFirst()
                .orElse(null);
            
            if (analytics == null) {
                logger.warn("⚠️ [REFUND] No ShopAnalytics found for this order");
                return;
            }
            
            // Tính lại chiết khấu cần hoàn
            Shop shop = order.getShop();
            if (shop != null && shop.getCommissionPercentage() != null) {
                BigDecimal commissionPercentage = shop.getCommissionPercentage();
                
                // Tính chiết khấu cần hoàn cho đơn này
                BigDecimal commissionToRefund = order.getTotalAmount()
                    .multiply(commissionPercentage)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                
                // Tính lại tiền shop nhận
                BigDecimal shopNetRevenueToRefund = order.getTotalAmount()
                    .subtract(commissionToRefund);
                
                logger.info("📍 [REFUND] Calculating refund amounts");
                logger.info("   Commission to refund: {}", commissionToRefund);
                logger.info("   Shop net revenue to refund: {}", shopNetRevenueToRefund);
                
                // Trừ đi từ ShopAnalytics (hoàn lại tiền)
                analytics.setCommissionAmount(
                    analytics.getCommissionAmount() != null ? 
                    analytics.getCommissionAmount() : BigDecimal.ZERO
                );
                analytics.setCommissionAmount(
                    analytics.getCommissionAmount().subtract(commissionToRefund)
                );
                
                analytics.setShopNetRevenue(
                    analytics.getShopNetRevenue() != null ? 
                    analytics.getShopNetRevenue() : BigDecimal.ZERO
                );
                analytics.setShopNetRevenue(
                    analytics.getShopNetRevenue().subtract(shopNetRevenueToRefund)
                );
                
                // Giảm số lượng đơn hàng
                analytics.setTotalOrders(Math.max(0, analytics.getTotalOrders() - 1));
                
                // Giảm doanh thu tổng
                analytics.setTotalRevenue(
                    analytics.getTotalRevenue().subtract(order.getTotalAmount())
                );
                
                logger.info("📍 [REFUND] Updating ShopAnalytics");
                logger.info("   New commission amount: {}", analytics.getCommissionAmount());
                logger.info("   New shop net revenue: {}", analytics.getShopNetRevenue());
                logger.info("   New total orders: {}", analytics.getTotalOrders());
                logger.info("   New total revenue: {}", analytics.getTotalRevenue());
                
                shopAnalyticsRepository.save(analytics);
                
                logger.info("✅ [REFUND] Commission refund completed successfully");
                logger.info("═══════════════════════════════════════════════════════════");
            }
        } catch (Exception e) {
            logger.error("❌ [REFUND] Error refunding commission: {}", e.getMessage());
            logger.error("═══════════════════════════════════════════════════════════");
            throw new RuntimeException("Error refunding commission", e);
        }
    }

    private void updateShopAnalytics(Integer shopId, LocalDate date, Order order) {
        logger.info("🔵 [DailyAnalytics] updateShopAnalytics - START");
        logger.info("   shopId: {}, orderDate: {}, orderId: {}", shopId, date, order.getId());
        logger.info("   totalAmount: {}", order.getTotalAmount());
        
        try {
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
            if (shop == null) {
                logger.warn("⚠️  [DailyAnalytics] Shop is NULL - cannot calculate commission");
                return;
            }
            
            logger.info("📍 [DailyAnalytics] Shop found: {} (ID: {})", shop.getShopName(), shop.getId());
            
            BigDecimal commissionPercentage = (shop.getCommissionPercentage() != null) 
                ? shop.getCommissionPercentage() 
                : BigDecimal.ZERO;
            
            logger.info("📍 [DailyAnalytics] Calculating commission");
            logger.info("   Shop commission percentage value: {}", commissionPercentage);
            logger.info("   shop.getCommissionPercentage() raw value: {}", shop.getCommissionPercentage());
            
            if (commissionPercentage.compareTo(BigDecimal.ZERO) == 0) {
                logger.info("ℹ️  [DailyAnalytics] Commission percentage is 0% - no commission charged");
                logger.warn("   ⚠️ If this is unexpected, check if admin set commission for this shop!");
                logger.warn("   → Go to /admin/shops/{}/commission and set it", shopId);
            }
            
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
            
            logger.info("✅ [DailyAnalytics] ShopAnalytics object prepared for save");
            logger.info("   Cumulative commission: {}", analytics.getCommissionAmount());
            logger.info("   Cumulative shop net revenue: {}", analytics.getShopNetRevenue());
            logger.info("   Total revenue: {}", analytics.getTotalRevenue());
            logger.info("   Total orders: {}", analytics.getTotalOrders());
            
            logger.info("📍 [DailyAnalytics] SAVING ShopAnalytics to database...");
            logger.info("   Analytics ID: {}, Shop ID: {}, Period: {} to {}", 
                       analytics.getId(), shopId, analytics.getPeriodStart(), analytics.getPeriodEnd());
            
            ShopAnalytics savedAnalytics = shopAnalyticsRepository.save(analytics);
            
            logger.info("✅ [DailyAnalytics] ShopAnalytics SAVED successfully!");
            logger.info("   Saved analytics ID: {}", savedAnalytics.getId());
            logger.info("✅ [DailyAnalytics] updateShopAnalytics - SUCCESS");
            
        } catch (Exception e) {
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("❌ [DailyAnalytics] ERROR SAVING SHOPANALYTICS");
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("🔴 Exception Type: {}", e.getClass().getName());
            logger.error("🔴 Exception Message: {}", e.getMessage());
            logger.error("🔴 Root Cause: ");
            
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 5) {
                logger.error("   [{} levels deep] {}: {}", depth, cause.getClass().getName(), cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
            
            logger.error("🔴 Full Stack Trace:");
            logger.error("", e);
            logger.error("═════════════════════════════════════════════════════════════");
            
            // ⚠️ DO NOT RE-THROW - we don't want to rollback the entire transaction
            // Just log the error for debugging
            logger.error("ℹ️  IMPORTANT: This error means commission data was NOT saved!");
            logger.error("ℹ️  Check the above error message to understand why");
        }
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
