package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private ConversionAnalyticsRepository conversionAnalyticsRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ShopRepository shopRepository;

    @PostMapping("/track/view")
    public ResponseEntity<?> trackProductView(@RequestBody Map<String, Object> payload) {
        try {
            Integer productId = Integer.parseInt(payload.get("productId").toString());
            Product product = productRepository.findById(productId).orElse(null);
            
            if (product == null || product.getShop() == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }
            
            // Update ConversionAnalytics for current week
            updateConversionMetric(product.getShop().getId(), "view");
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error tracking view");
        }
    }

    @PostMapping("/track/cart")
    public ResponseEntity<?> trackAddToCart(@RequestBody Map<String, Object> payload) {
        try {
            Integer productId = Integer.parseInt(payload.get("productId").toString());
            Product product = productRepository.findById(productId).orElse(null);
            
            if (product == null || product.getShop() == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }
            
            // Update ConversionAnalytics for current week
            updateConversionMetric(product.getShop().getId(), "cart");
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error tracking cart event");
        }
    }

    @PostMapping("/track/checkout")
    public ResponseEntity<?> trackCheckout(@RequestBody(required = false) Map<String, Object> payload) {
        try {
            // Lấy productId từ payload
            if (payload == null || !payload.containsKey("productId")) {
                return ResponseEntity.badRequest().body("productId required");
            }
            
            Integer productId = Integer.parseInt(payload.get("productId").toString());
            Product product = productRepository.findById(productId).orElse(null);
            
            if (product == null || product.getShop() == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }
            
            // Lấy shopId từ product và track
            updateConversionMetric(product.getShop().getId(), "checkout");
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error tracking checkout: " + e.getMessage());
        }
    }
    @PostMapping("/track/checkout-batch")
    public ResponseEntity<?> trackCheckoutBatch(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("productIds")) {
                return ResponseEntity.badRequest().body("productIds required");
            }
            
            @SuppressWarnings("unchecked")
            List<Integer> productIds = (List<Integer>) payload.get("productIds");
            
            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.badRequest().body("productIds cannot be empty");
            }
            
            // Lấy danh sách unique shop IDs từ các sản phẩm
            Set<Integer> shopIds = new HashSet<>();
            
            for (Integer productId : productIds) {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null && product.getShop() != null) {
                    shopIds.add(product.getShop().getId());
                }
            }
            
            // Track checkout cho mỗi shop
            for (Integer shopId : shopIds) {
                updateConversionMetric(shopId, "checkout");
            }
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Checkout tracked successfully",
                    "shopsTracked", shopIds.size()
                ));
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body("Error tracking checkout batch: " + e.getMessage());
        }
    }


    @PostMapping("/track/checkout-by-product")
    public ResponseEntity<?> trackCheckoutByProduct(@RequestBody Map<String, Object> payload) {
        try {
            Integer productId = Integer.parseInt(payload.get("productId").toString());
            Product product = productRepository.findById(productId).orElse(null);
            
            if (product == null || product.getShop() == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }
            
            // Update ConversionAnalytics for current week
            updateConversionMetric(product.getShop().getId(), "checkout");
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error tracking checkout");
        }
    }

    private void updateConversionMetric(Integer shopId, String metricType) {
        LocalDate today = LocalDate.now();
        
        // Track by DAY for granular data
        ConversionAnalytics analytics = conversionAnalyticsRepository.findAll().stream()
            .filter(a -> a.getShop() != null && a.getShop().getId().equals(shopId))
            .filter(a -> "DAY".equals(a.getPeriodType()))
            .filter(a -> today.equals(a.getPeriodStart()))
            .findFirst()
            .orElseGet(() -> {
                ConversionAnalytics newAnalytics = new ConversionAnalytics();
                newAnalytics.setShop(shopRepository.findById(shopId).orElse(null));
                newAnalytics.setPeriodType("DAY");
                newAnalytics.setPeriodStart(today);
                newAnalytics.setPeriodEnd(today);
                return newAnalytics;
            });
        
        // Increment appropriate counter (handle nulls)
        switch (metricType) {
            case "view":
                analytics.setViewCount((analytics.getViewCount() != null ? analytics.getViewCount() : 0) + 1);
                break;
            case "cart":
                analytics.setAddToCartCount((analytics.getAddToCartCount() != null ? analytics.getAddToCartCount() : 0) + 1);
                break;
            case "checkout":
                analytics.setCheckoutCount((analytics.getCheckoutCount() != null ? analytics.getCheckoutCount() : 0) + 1);
                break;
        }
        
        conversionAnalyticsRepository.save(analytics);
    }
}
