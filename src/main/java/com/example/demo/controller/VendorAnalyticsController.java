package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.DailyAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendor/analytics")
public class VendorAnalyticsController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private DailyAnalyticsService dailyAnalyticsService;

    /**
     * Backfill analytics for vendor's shop
     * Call this once to populate historical data for your shop
     */
    @PostMapping("/backfill")
    public ResponseEntity<?> backfillAnalytics(@SessionAttribute("currentUser") User currentUser) {
        try {
            // Get vendor's shop
            Integer userId = currentUser.getUserId();
            Shop shop = shopRepository.findByVendorUserId(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found for user: " + userId));
            Integer shopId = shop.getId();
            
            // Get all delivered/completed orders for this shop
            List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getShop() != null && o.getShop().getId().equals(shopId))
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> "Delivered".equalsIgnoreCase(o.getOrderStatus()) || 
                            "Hoàn thành".equalsIgnoreCase(o.getOrderStatus()))
                .collect(Collectors.toList());
            
            // Group by shop and date
            Map<String, List<Order>> groupedOrders = new HashMap<>();
            for (Order order : completedOrders) {
                LocalDate orderDate = order.getOrderDate().toLocalDate();
                String key = order.getShop().getId() + "_" + orderDate.toString();
                groupedOrders.computeIfAbsent(key, k -> new ArrayList<>()).add(order);
            }
            
            // Recalculate analytics for each date
            int processedDates = 0;
            for (Map.Entry<String, List<Order>> entry : groupedOrders.entrySet()) {
                String[] parts = entry.getKey().split("_");
                LocalDate date = LocalDate.parse(parts[1]);
                
                dailyAnalyticsService.recalculateDailyAnalytics(shopId, date);
                processedDates++;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalOrders", completedOrders.size());
            response.put("datesProcessed", processedDates);
            response.put("message", "Analytics backfilled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
