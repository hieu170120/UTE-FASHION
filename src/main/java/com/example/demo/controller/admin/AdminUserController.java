package com.example.demo.controller.admin;

import com.example.demo.dto.UserManagementDTO;
import com.example.demo.dto.UserOrderHistoryDTO;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller quản lý User cho Admin
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    /**
     * Trang danh sách user với tìm kiếm và phân trang
     */
    @GetMapping
    public String listUsers(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) Boolean isActive) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementDTO> userPage = adminService.searchUsers(search, isActive, pageable);
        
        // Lấy thống kê tổng quan
        Map<String, Object> statistics = adminService.getUserStatistics();
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("searchQuery", search);
        model.addAttribute("isActiveFilter", isActive);
        model.addAttribute("statistics", statistics);
        
        return "admin/user-management";
    }

    /**
     * API khóa tài khoản user
     */
    @PutMapping("/{id}/lock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lockUser(@PathVariable("id") Integer userId) {
        System.out.println("DEBUG: AdminUserController.lockUser called for userId=" + userId);
        
        try {
            adminService.lockUser(userId);
            System.out.println("DEBUG: Successfully locked user " + userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã khóa tài khoản thành công"
            ));
        } catch (Exception e) {
            System.err.println("DEBUG: Error locking user " + userId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * API mở khóa tài khoản user
     */
    @PutMapping("/{id}/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlockUser(@PathVariable("id") Integer userId) {
        System.out.println("DEBUG: AdminUserController.unlockUser called for userId=" + userId);
        
        try {
            adminService.unlockUser(userId);
            System.out.println("DEBUG: Successfully unlocked user " + userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã mở khóa tài khoản thành công"
            ));
        } catch (Exception e) {
            System.err.println("DEBUG: Error unlocking user " + userId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AdminUserController is working!");
    }

    /**
     * Test endpoint cho orders
     */
    @GetMapping("/{id}/orders/test")
    @ResponseBody
    public ResponseEntity<String> testOrders(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok("Orders endpoint working for user: " + userId);
    }

    /**
     * API lấy lịch sử đơn hàng của user
     */
    @GetMapping("/{id}/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserOrderHistory(
            @PathVariable("id") Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        System.out.println("DEBUG: AdminUserController.getUserOrderHistory called for userId=" + userId + ", page=" + page + ", size=" + size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserOrderHistoryDTO> orderPage = adminService.getUserOrderHistory(userId, pageable);
            Map<String, Object> statistics = adminService.getUserOrderStatistics(userId);
            
            System.out.println("DEBUG: Successfully retrieved " + orderPage.getTotalElements() + " orders for user " + userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", orderPage.getContent(),
                "totalPages", orderPage.getTotalPages(),
                "currentPage", orderPage.getNumber(),
                "totalElements", orderPage.getTotalElements(),
                "statistics", statistics
            ));
        } catch (Exception e) {
            System.err.println("DEBUG: Error getting user order history for " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * API lấy thống kê đơn hàng của user
     */
    @GetMapping("/{id}/orders/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserOrderStatistics(@PathVariable("id") Integer userId) {
        System.out.println("DEBUG: AdminUserController.getUserOrderStatistics called for userId=" + userId);
        
        try {
            Map<String, Object> statistics = adminService.getUserOrderStatistics(userId);
            System.out.println("DEBUG: Successfully retrieved statistics for user " + userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", statistics
            ));
        } catch (Exception e) {
            System.err.println("DEBUG: Error getting user order statistics for " + userId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
