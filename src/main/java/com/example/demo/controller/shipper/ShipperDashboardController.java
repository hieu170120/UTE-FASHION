package com.example.demo.controller.shipper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ShipperCancelHistoryDTO;
import com.example.demo.dto.ShipperOrderHistoryItemDTO;
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
import java.time.LocalDateTime;
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
        
        // Lấy lịch sử hủy của SHIPPER HIỆN TẠI này
        List<ShipperCancelHistoryDTO> cancelHistory = orderManagementService.getShipperCancelHistory(shipperId);
        
        // KHÔNG LỌC BỞ - CÙNG 1 ĐƠN CÓ THỂ XUẤT HIỆN NHIỀU LẦN VỚI CÁC TRẠNG THÁI KHÁC NHAU
        // Ví dụ: Đơn #123 có thể có:
        //  - 1 bản ghi "Shipper hủy" (lần 1)
        //  - 1 bản ghi "Confirmed" (lần 2 - admin giao lại)
        //  - 1 bản ghi "Shipper hủy" (lần 3 - hủy lại)
        //  - ...
        List<OrderDTO> filteredOrders = new java.util.ArrayList<>(allOrders);
        
        long totalDelivered = filteredOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
        // Số đơn shipper hủy (vẫn giữ nguyên tất cả lịch sử hủy)
        long totalCancelled = cancelHistory.size();
        long totalReturned = filteredOrders.stream().filter(o -> "Returned".equals(o.getOrderStatus())).count();
        // Doanh thu = Tổng phí vận chuyển của các đơn đã giao thành công
        double totalRevenue = filteredOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getShippingFee() != null)
            .mapToDouble(o -> o.getShippingFee().doubleValue())
            .sum();
        
        // Merge orders và cancelHistory thành một list chung để hiển thị theo thời gian
        List<ShipperOrderHistoryItemDTO> combinedHistory = new java.util.ArrayList<>();
        
        // Thêm TẤT CẢ các đơn thường
        for (OrderDTO order : filteredOrders) {
            ShipperOrderHistoryItemDTO item = new ShipperOrderHistoryItemDTO();
            item.setOrderId(order.getId());
            item.setOrderNumber(order.getOrderNumber());
            item.setRecipientName(order.getRecipientName());
            item.setPhoneNumber(order.getPhoneNumber());
            item.setShippingAddress(order.getShippingAddress());
            item.setTotalAmount(order.getTotalAmount());
            item.setOrderStatus(order.getOrderStatus());
            
            // Lấy thời gian mới nhất tùy theo trạng thái
            LocalDateTime displayDate = order.getOrderDate(); // Mặc định
            if (order.getDeliveredAt() != null) {
                displayDate = order.getDeliveredAt();
            } else if (order.getShippedAt() != null) {
                displayDate = order.getShippedAt();
            } else if (order.getConfirmedAt() != null) {
                displayDate = order.getConfirmedAt();
            }
            item.setDisplayDate(displayDate);
            item.setShipperCancelled(false);
            combinedHistory.add(item);
        }
        
        // Thêm các đơn shipper hủy (vẫn giữ tất cả trong lịch sử)
        for (ShipperCancelHistoryDTO cancel : cancelHistory) {
            ShipperOrderHistoryItemDTO item = new ShipperOrderHistoryItemDTO();
            item.setOrderId(cancel.getOrderId());
            item.setOrderNumber(cancel.getOrderNumber());
            item.setRecipientName(cancel.getRecipientName());
            item.setPhoneNumber(null);
            item.setShippingAddress(null);
            item.setTotalAmount(cancel.getTotalAmount());
            item.setOrderStatus("ShipperCancelled");
            item.setDisplayDate(cancel.getCancelledAt());
            item.setCancelReason(cancel.getReason());
            item.setShipperCancelled(true);
            combinedHistory.add(item);
        }
        
        // Sort theo thời gian giảm dần
        combinedHistory.sort((o1, o2) -> o2.getDisplayDate().compareTo(o1.getDisplayDate()));
        
        model.addAttribute("combinedHistory", combinedHistory);
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
        
        // Lấy lịch sử hủy của shipper
        List<ShipperCancelHistoryDTO> cancelHistory = orderManagementService.getShipperCancelHistory(shipperId);
        
        // Sort by date descending for each category
        List<OrderDTO> deliveringOrders = allOrders.stream()
            .filter(o -> "Shipping".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        List<OrderDTO> deliveredOrders = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        List<OrderDTO> returnedOrders = allOrders.stream()
            .filter(o -> "Returned".equals(o.getOrderStatus()))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .toList();
        
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelHistory);
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
        
        // Lấy lịch sử hủy của shipper
        List<ShipperCancelHistoryDTO> cancelHistory = orderManagementService.getShipperCancelHistory(shipperId);
        
        // Calculate statistics
        long totalOrders = allOrders.size();
        long deliveredCount = allOrders.stream().filter(o -> "Delivered".equals(o.getOrderStatus())).count();
        
        // Phân biệt cancelled by customer vs shipper
        long customerCancelledCount = allOrders.stream()
            .filter(o -> "Cancelled".equals(o.getOrderStatus()))
            .filter(o -> "CUSTOMER".equals(o.getCancelledBy()))
            .count();
        // Số đơn shipper hủy từ lịch sử hủy
        long cancelledCount = cancelHistory.size();
        
        long returnedCount = allOrders.stream().filter(o -> "Returned".equals(o.getOrderStatus())).count();
        long shippingCount = allOrders.stream().filter(o -> "Shipping".equals(o.getOrderStatus())).count();
        long confirmedCount = allOrders.stream().filter(o -> "Confirmed".equals(o.getOrderStatus())).count();
        
        // Doanh thu = Tổng phí vận chuyển của các đơn đã giao thành công
        double totalRevenue = allOrders.stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> o.getShippingFee() != null)
            .mapToDouble(o -> o.getShippingFee().doubleValue())
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
        model.addAttribute("customerCancelledCount", customerCancelledCount);
        model.addAttribute("shipperCancelledCount", cancelledCount);
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
        
        // Merge orders và cancelHistory thành một list chung cho recent orders
        // KHÔNG LỌC BỞ - cho phép cùng 1 đơn xuất hiện nhiều lần
        List<ShipperOrderHistoryItemDTO> recentCombined = new java.util.ArrayList<>();
        
        // Thêm TẤT CẢ các đơn thường
        for (OrderDTO order : allOrders) {
            ShipperOrderHistoryItemDTO item = new ShipperOrderHistoryItemDTO();
            item.setOrderId(order.getId());
            item.setOrderNumber(order.getOrderNumber());
            item.setRecipientName(order.getRecipientName());
            item.setTotalAmount(order.getTotalAmount());
            item.setOrderStatus(order.getOrderStatus());
            
            // Lấy thời gian mới nhất tùy theo trạng thái
            java.time.LocalDateTime displayDate = order.getOrderDate(); // Mặc định
            if (order.getDeliveredAt() != null) {
                displayDate = order.getDeliveredAt();
            } else if (order.getShippedAt() != null) {
                displayDate = order.getShippedAt();
            } else if (order.getConfirmedAt() != null) {
                displayDate = order.getConfirmedAt();
            }
            item.setDisplayDate(displayDate);
            item.setShipperCancelled(false);
            recentCombined.add(item);
        }
        
        // Thêm các đơn shipper hủy
        for (ShipperCancelHistoryDTO cancel : cancelHistory) {
            ShipperOrderHistoryItemDTO item = new ShipperOrderHistoryItemDTO();
            item.setOrderId(cancel.getOrderId());
            item.setOrderNumber(cancel.getOrderNumber());
            item.setRecipientName(cancel.getRecipientName());
            item.setTotalAmount(cancel.getTotalAmount());
            item.setOrderStatus("ShipperCancelled");
            item.setDisplayDate(cancel.getCancelledAt());
            item.setShipperCancelled(true);
            recentCombined.add(item);
        }
        
        // Sort theo thời gian giảm dần và lấy 10 đơn gần nhất
        List<ShipperOrderHistoryItemDTO> recentOrders = recentCombined.stream()
            .sorted((o1, o2) -> o2.getDisplayDate().compareTo(o1.getDisplayDate()))
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
