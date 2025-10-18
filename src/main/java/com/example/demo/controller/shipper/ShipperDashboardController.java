package com.example.demo.controller.shipper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.ShipperService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/shipper")
public class ShipperDashboardController {

    @Autowired
    private OrderManagementService orderManagementService;

    @Autowired
    private ShipperService shipperService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Trang chủ dashboard - Main layout with sidebar
     */
    @GetMapping({"", "/"})
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Pass data to main layout
        model.addAttribute("shipperName", currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
        Long unreadCount = notificationService.getUnreadNotificationCount(shipperId);
        model.addAttribute("unreadNotifications", unreadCount);
        
        return "shipper/dashboard-main";
    }
    
    /**
     * AJAX endpoint - Dashboard content
     */
    @GetMapping("/dashboard/content")
    public String dashboardContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Statistics
        model.addAttribute("assignedCount", orderManagementService.getShipperAssignedOrders(shipperId).size());
        model.addAttribute("deliveringCount", orderManagementService.getShipperDeliveringOrders(shipperId).size());
        model.addAttribute("deliveredCount", getDeliveredTodayCount(shipperId));
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        // Recent orders (top 5)
        List<OrderDTO> recentOrders = orderManagementService.getShipperAssignedOrders(shipperId);
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }
        model.addAttribute("recentOrders", recentOrders);
        
        return "shipper/fragments/dashboard-content";
    }
    
    /**
     * AJAX endpoint - Orders list content
     */
    @GetMapping("/orders/content")
    public String ordersContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        List<OrderDTO> assignedOrders = orderManagementService.getShipperAssignedOrders(shipperId);
        List<OrderDTO> deliveringOrders = orderManagementService.getShipperDeliveringOrders(shipperId);
        
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        return "shipper/fragments/orders-content";
    }
    
    /**
     * AJAX endpoint - History content
     */
    @GetMapping("/history/content")
    public String historyContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Get all orders for this shipper
        List<OrderDTO> allOrders = orderManagementService.getShipperAllOrders(shipperId);
        
        long totalDelivered = allOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
        long totalCancelled = allOrders.stream().filter(o -> "Cancelled".equals(o.getOrderStatus())).count();
        double totalRevenue = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getTotalAmount() != null)
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        
        model.addAttribute("orders", allOrders);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalCancelled", totalCancelled);
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "shipper/fragments/history-content";
    }
    
    /**
     * AJAX endpoint - Profile content
     */
    @GetMapping("/profile/content")
    public String profileContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        model.addAttribute("shipper", shipperService.getShipperById(shipperId));
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        return "shipper/fragments/profile-content";
    }
    
    /**
     * AJAX endpoint - Overview content (Tổng quan)
     */
    @GetMapping("/overview/content")
    public String overviewContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Get shipper info
        model.addAttribute("shipper", shipperService.getShipperById(shipperId));
        
        // Statistics
        model.addAttribute("assignedCount", orderManagementService.getShipperAssignedOrders(shipperId).size());
        model.addAttribute("deliveringCount", orderManagementService.getShipperDeliveringOrders(shipperId).size());
        model.addAttribute("deliveredCount", getDeliveredTodayCount(shipperId));
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        // Recent orders
        List<OrderDTO> recentOrders = orderManagementService.getShipperAssignedOrders(shipperId);
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }
        model.addAttribute("recentOrders", recentOrders);
        
        return "shipper/fragments/overview-content";
    }
    
    /**
     * AJAX endpoint - Delivering content (Đơn đang giao)
     */
    @GetMapping("/delivering/content")
    public String deliveringContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Get all orders for this shipper
        List<OrderDTO> allOrders = orderManagementService.getShipperAllOrders(shipperId);
        
        List<OrderDTO> deliveringOrders = allOrders.stream()
            .filter(o -> "Shipping".equals(o.getOrderStatus()))
            .toList();
        List<OrderDTO> deliveredOrders = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .toList();
        List<OrderDTO> cancelledOrders = allOrders.stream()
            .filter(o -> "Cancelled".equals(o.getOrderStatus()))
            .toList();
        
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        
        return "shipper/fragments/delivering-content";
    }
    
    /**
     * AJAX endpoint - Stats content (Thống kê)
     */
    @GetMapping("/stats/content")
    public String statsContent(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "error/403";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Get all orders for this shipper
        List<OrderDTO> allOrders = orderManagementService.getShipperAllOrders(shipperId);
        
        // Calculate statistics
        long totalOrders = allOrders.size();
        long deliveredCount = allOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
        long cancelledCount = allOrders.stream().filter(o -> "Cancelled".equals(o.getOrderStatus())).count();
        double totalRevenue = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getTotalAmount() != null)
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        double successRate = totalOrders > 0 ? (deliveredCount * 100.0 / totalOrders) : 0;
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("successRate", successRate);
        model.addAttribute("recentOrders", allOrders.stream().limit(10).toList());
        
        return "shipper/fragments/stats-content";
    }


    // DEPRECATED: Use dashboard AJAX endpoints /history/content and /profile/content instead
    // /**
    //  * Lịch sử giao hàng (đã giao + đã hủy)
    //  */
    // @GetMapping("/history")
    // public String history(@RequestParam(defaultValue = "0") int page,
    //                      @RequestParam(required = false) String status,
    //                      @RequestParam(required = false) String fromDate,
    //                      @RequestParam(required = false) String toDate,
    //                      Model model, 
    //                      HttpSession session) {
    //     User currentUser = (User) session.getAttribute("currentUser");
    //     if (!isShipper(currentUser)) {
    //         return "redirect:/login";
    //     }
    //
    //     Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
    //     
    //     // TODO: Implement filtering and pagination in service
    //     // For now, just get all orders
    //     List<OrderDTO> allOrders = orderManagementService.getCustomerOrders(currentUser.getUserId());
    //     
    //     // Filter by status if provided
    //     if (status != null && !status.isEmpty()) {
    //         allOrders = allOrders.stream()
    //             .filter(o -> status.equals(o.getOrderStatus()))
    //             .toList();
    //     }
    //     
    //     // Statistics
    //     long totalDelivered = allOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
    //     long totalCancelled = allOrders.stream().filter(o -> "Cancelled".equals(o.getOrderStatus())).count();
    //     double totalRevenue = allOrders.stream()
    //         .filter(o -> "Delivered".equals(o.getOrderStatus()))
    //         .filter(o -> o.getTotalAmount() != null)
    //         .mapToDouble(o -> o.getTotalAmount().doubleValue())
    //         .sum();
    //     
    //     model.addAttribute("orders", allOrders);
    //     model.addAttribute("totalDelivered", totalDelivered);
    //     model.addAttribute("totalCancelled", totalCancelled);
    //     model.addAttribute("totalRevenue", totalRevenue);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", 1);
    //     
    //     return "shipper/history";
    // }
    //
    // /**
    //  * Trang profile shipper
    //  */
    // @GetMapping("/profile")
    // public String profile(Model model, HttpSession session) {
    //     User currentUser = (User) session.getAttribute("currentUser");
    //     if (!isShipper(currentUser)) {
    //         return "redirect:/login";
    //     }
    //
    //     Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
    //     model.addAttribute("shipper", shipperService.getShipperById(shipperId));
    //     model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
    //     
    //     return "shipper/profile";
    // }

    // Helper methods
    private boolean isShipper(User user) {
        return user != null && 
               user.getRoles() != null && 
               user.getRoles().stream().anyMatch(role -> "SHIPPER".equals(role.getRoleName()));
    }

    private long getDeliveredTodayCount(Integer shipperId) {
        // TODO: Implement in service to get delivered orders for today
        return 0;
    }
}
