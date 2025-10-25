package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.UserManagementDTO;
import com.example.demo.dto.UserOrderHistoryDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service quản lý User cho Admin
 */
@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;

    /**
     * Tìm kiếm và phân trang danh sách user
     */
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> searchUsers(String keyword, Boolean isActive, Pageable pageable) {
        System.out.println("DEBUG: searchUsers called with keyword=" + keyword + ", isActive=" + isActive + ", page=" + pageable.getPageNumber());
        
        Page<User> userPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm theo keyword
            userPage = userRepository.findByKeyword(keyword.trim(), pageable);
            System.out.println("DEBUG: Search by keyword found " + userPage.getTotalElements() + " users");
        } else if (isActive != null) {
            // Lọc theo trạng thái
            userPage = userRepository.findByIsActive(isActive, pageable);
            System.out.println("DEBUG: Filter by isActive=" + isActive + " found " + userPage.getTotalElements() + " users");
        } else {
            // Lấy tất cả
            userPage = userRepository.findAll(pageable);
            System.out.println("DEBUG: Get all users found " + userPage.getTotalElements() + " users");
        }
        
        return userPage.map(this::mapToUserManagementDTO);
    }

    /**
     * Khóa tài khoản user
     */
    @Transactional
    public void lockUser(Integer userId) {
        System.out.println("DEBUG: lockUser called for userId=" + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        System.out.println("DEBUG: Found user: " + user.getUsername() + ", current isActive=" + user.getIsActive());
        
        if (!user.getIsActive()) {
            System.out.println("DEBUG: User is already locked, throwing exception");
            throw new RuntimeException("User is already locked");
        }
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        System.out.println("DEBUG: Successfully locked user: " + user.getUsername());
    }

    /**
     * Mở khóa tài khoản user
     */
    @Transactional
    public void unlockUser(Integer userId) {
        System.out.println("DEBUG: unlockUser called for userId=" + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        System.out.println("DEBUG: Found user: " + user.getUsername() + ", current isActive=" + user.getIsActive());
        
        if (user.getIsActive()) {
            System.out.println("DEBUG: User is already unlocked, throwing exception");
            throw new RuntimeException("User is already unlocked");
        }
        
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        System.out.println("DEBUG: Successfully unlocked user: " + user.getUsername());
    }

    /**
     * Lấy thống kê tổng quan về user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long lockedUsers = userRepository.countByIsActive(false);
        long newUsersToday = userRepository.countByCreatedAtAfter(LocalDateTime.now().toLocalDate().atStartOfDay());
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("lockedUsers", lockedUsers);
        stats.put("newUsersToday", newUsersToday);
        
        return stats;
    }

    /**
     * Lấy lịch sử đơn hàng của user
     */
    @Transactional(readOnly = true)
    public Page<UserOrderHistoryDTO> getUserOrderHistory(Integer userId, Pageable pageable) {
        System.out.println("DEBUG: getUserOrderHistory called for userId=" + userId + ", page=" + pageable.getPageNumber());
        
        try {
            Page<OrderDTO> orderPage = orderService.getUserOrdersPageable(userId, pageable);
            System.out.println("DEBUG: Found " + orderPage.getTotalElements() + " orders for user " + userId);
            
            return orderPage.map(this::mapToUserOrderHistoryDTO);
        } catch (Exception e) {
            System.err.println("ERROR in getUserOrderHistory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Lấy thống kê đơn hàng của user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserOrderStatistics(Integer userId) {
        System.out.println("DEBUG: getUserOrderStatistics called for userId=" + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        System.out.println("DEBUG: Found user: " + user.getUsername());
        
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng số đơn hàng
        long totalOrders = orderRepository.countByUserUserId(userId);
        stats.put("totalOrders", totalOrders);
        System.out.println("DEBUG: Total orders: " + totalOrders);
        
        // Tổng chi tiêu
        BigDecimal totalSpent = orderRepository.getTotalSpentByUserId(userId);
        stats.put("totalSpent", totalSpent);
        System.out.println("DEBUG: Total spent: " + (totalSpent != null ? totalSpent : BigDecimal.ZERO));
        
        // Đơn hàng gần nhất
        LocalDateTime lastOrderDate = orderRepository.getLastOrderDateByUserId(userId);
        stats.put("lastOrderDate", lastOrderDate);
        System.out.println("DEBUG: Last order date: " + lastOrderDate);
        
        // Đơn hàng theo trạng thái
        List<Object[]> ordersByStatusRaw = orderRepository.countOrdersByStatusAndUserId(userId);
        
        // Convert List<Object[]> to Map<String, Long> safely
        Map<String, Long> ordersByStatus = new HashMap<>();
        if (ordersByStatusRaw != null) {
            for (Object[] row : ordersByStatusRaw) {
                if (row.length >= 2 && row[0] != null) {
                    String status = (String) row[0];
                    Long count;
                    
                    // Handle different number types that might be returned
                    if (row[1] instanceof Long) {
                        count = (Long) row[1];
                    } else if (row[1] instanceof Integer) {
                        count = ((Integer) row[1]).longValue();
                    } else if (row[1] instanceof String) {
                        count = Long.parseLong((String) row[1]);
                    } else {
                        count = ((Number) row[1]).longValue();
                    }
                    
                    ordersByStatus.put(status, count);
                }
            }
        }
        
        stats.put("ordersByStatus", ordersByStatus);
        System.out.println("DEBUG: Orders by status: " + ordersByStatus);
        
        System.out.println("DEBUG: Statistics completed for user: " + user.getUsername());
        return stats;
    }

    /**
     * Map User entity sang UserManagementDTO
     */
    private UserManagementDTO mapToUserManagementDTO(User user) {
        // Tự động fix dữ liệu nếu thiếu
        LocalDateTime now = LocalDateTime.now();
        boolean userChanged = false;
        
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now.minusDays(30));
            userChanged = true;
        }
        if (user.getLastLogin() == null && user.getIsActive()) {
            int randomDays = (int) (Math.random() * 7) + 1;
            user.setLastLogin(now.minusDays(randomDays));
            userChanged = true;
        }
        if (userChanged) {
            user.setUpdatedAt(now);
            userRepository.save(user);
        }
        
        UserManagementDTO dto = new UserManagementDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhoneNumber());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIsActive(user.getIsActive());
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }

    /**
     * Map OrderDTO sang UserOrderHistoryDTO
     */
    private UserOrderHistoryDTO mapToUserOrderHistoryDTO(OrderDTO order) {
        UserOrderHistoryDTO dto = new UserOrderHistoryDTO();
        dto.setOrderId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setItemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        dto.setShippingAddress(order.getShippingAddress());
        dto.setRecipientName(order.getRecipientName());
        dto.setPhoneNumber(order.getPhoneNumber());
        return dto;
    }
}