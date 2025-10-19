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
        
        // Get all orders for this shipper - Sort by date descending
        List<OrderDTO> allOrders = orderManagementService.getShipperAllOrders(shipperId).stream()
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        
        long totalDelivered = allOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
        long totalCancelled = allOrders.stream().filter(o -> "Cancelled".equals(o.getOrderStatus())).count();
        long totalReturned = allOrders.stream().filter(o -> "Returned".equals(o.getOrderStatus())).count();
        double totalRevenue = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getTotalAmount() != null)
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        
        model.addAttribute("orders", allOrders);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalCancelled", totalCancelled);
        model.addAttribute("totalReturned", totalReturned);
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
        
        // Sort by date descending for each category
        List<OrderDTO> deliveringOrders = allOrders.stream()
            .filter(o -> "Shipping".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        List<OrderDTO> deliveredOrders = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        List<OrderDTO> cancelledOrders = allOrders.stream()
            .filter(o -> "Cancelled".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        List<OrderDTO> returnedOrders = allOrders.stream()
            .filter(o -> "Returned".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("returnedOrders", returnedOrders);
        
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
        long returnedCount = allOrders.stream().filter(o -> "Returned".equals(o.getOrderStatus())).count();
        long shippingCount = allOrders.stream().filter(o -> "Shipping".equals(o.getOrderStatus())).count();
        long confirmedCount = allOrders.stream().filter(o -> "Confirmed".equals(o.getOrderStatus())).count();
        
        double totalRevenue = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getTotalAmount() != null)
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        double successRate = totalOrders > 0 ? (deliveredCount * 100.0 / totalOrders) : 0;
        
        // Calculate percentages for chart
        double deliveredPercent = totalOrders > 0 ? (deliveredCount * 100.0 / totalOrders) : 0;
        double cancelledPercent = totalOrders > 0 ? (cancelledCount * 100.0 / totalOrders) : 0;
        double returnedPercent = totalOrders > 0 ? (returnedCount * 100.0 / totalOrders) : 0;
        double shippingPercent = totalOrders > 0 ? (shippingCount * 100.0 / totalOrders) : 0;
        double confirmedPercent = totalOrders > 0 ? (confirmedCount * 100.0 / totalOrders) : 0;
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("successRate", successRate);
        model.addAttribute("deliveredPercent", deliveredPercent);
        model.addAttribute("cancelledPercent", cancelledPercent);
        model.addAttribute("returnedPercent", returnedPercent);
        model.addAttribute("shippingPercent", shippingPercent);
        model.addAttribute("confirmedPercent", confirmedPercent);
        
        // Recent orders - Sort by date descending and take 10
        List<OrderDTO> recentOrders = allOrders.stream()
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .limit(10)
            .toList();
        model.addAttribute("recentOrders", recentOrders);
        
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
        List<OrderDTO> allOrders = orderManagementService.getShipperAllOrders(shipperId);
        LocalDate today = LocalDate.now();
        
        return allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getDeliveredAt() != null && o.getDeliveredAt().toLocalDate().equals(today))
            .count();
    }
}
