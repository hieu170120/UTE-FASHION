package com.example.demo.controller.shipper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
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

    /**
     * Trang chủ dashboard
     */
    @GetMapping({"", "/"})
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // Statistics
        model.addAttribute("shipperName", currentUser.getUsername());
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
        
        return "shipper/index";
    }

    /**
     * Danh sách đơn được giao (chưa xác nhận)
     */
    @GetMapping("/orders/assigned")
    public String assignedOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        List<OrderDTO> assignedOrders = orderManagementService.getShipperAssignedOrders(shipperId);
        
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        return "shipper/orders-assigned";
    }

    /**
     * Xác nhận nhận đơn và bắt đầu giao
     */
    @PostMapping("/orders/{orderId}/confirm")
    public String confirmOrder(@PathVariable Integer orderId, 
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperConfirmOrder(orderId, shipperId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng và bắt đầu giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/shipper/orders/assigned";
    }

    /**
     * Hủy đơn hàng với lý do
     */
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId, 
                             @RequestParam String reason,
                             HttpSession session, 
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperCancelOrder(orderId, shipperId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn: " + e.getMessage());
        }
        
        return "redirect:/shipper/orders/assigned";
    }

    /**
     * Danh sách đơn đang giao (có đồng hồ đếm ngược)
     */
    @GetMapping("/orders/delivering")
    public String deliveringOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        List<OrderDTO> deliveringOrders = orderManagementService.getShipperDeliveringOrders(shipperId);
        
        model.addAttribute("deliveringOrders", deliveringOrders);
        
        return "shipper/orders-delivering";
    }

    /**
     * Lịch sử giao hàng (đã giao + đã hủy)
     */
    @GetMapping("/history")
    public String history(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String fromDate,
                         @RequestParam(required = false) String toDate,
                         Model model, 
                         HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        
        // TODO: Implement filtering and pagination in service
        // For now, just get all orders
        List<OrderDTO> allOrders = orderManagementService.getCustomerOrders(currentUser.getUserId());
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            allOrders = allOrders.stream()
                .filter(o -> status.equals(o.getOrderStatus()))
                .toList();
        }
        
        // Statistics
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
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1);
        
        return "shipper/history";
    }

    /**
     * Trang profile shipper
     */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (!isShipper(currentUser)) {
            return "redirect:/login";
        }

        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        model.addAttribute("shipper", shipperService.getShipperById(shipperId));
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        
        return "shipper/profile";
    }

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
