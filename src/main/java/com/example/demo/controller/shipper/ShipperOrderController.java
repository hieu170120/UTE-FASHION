package com.example.demo.controller.shipper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.ShipperService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shipper/orders")
public class ShipperOrderController {

    @Autowired
    private OrderManagementService orderManagementService;

    @Autowired
    private ShipperService shipperService;
    
    @Autowired
    private NotificationService notificationService;

    // DEPRECATED: Use dashboard AJAX endpoints instead
    // @GetMapping
    // public String listAssignedOrders(Model model, HttpSession session) {
    //     User currentUser = (User) session.getAttribute("currentUser");
    //     if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
    //         return "redirect:/login";
    //     }
    //     Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
    //     List<OrderDTO> assignedOrders = orderManagementService.getShipperAssignedOrders(shipperId);
    //     List<OrderDTO> deliveringOrders = orderManagementService.getShipperDeliveringOrders(shipperId);
    //     Long unreadNotificationCount = notificationService.getUnreadNotificationCount(shipperId);
    //     
    //     model.addAttribute("shipperName", currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
    //     model.addAttribute("assignedOrders", assignedOrders);
    //     model.addAttribute("deliveringOrders", deliveringOrders);
    //     model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
    //     model.addAttribute("unreadCount", unreadNotificationCount);
    //     return "shipper/order/list";
    // }

    // DEPRECATED: Use dashboard AJAX endpoints instead
    // @GetMapping("/stats")
    // public String orderStats(Model model, HttpSession session) {
    //     User currentUser = (User) session.getAttribute("currentUser");
    //     if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
    //         return "redirect:/login";
    //     }
    //     Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
    //     long assignedCount = orderManagementService.getShipperAssignedOrders(shipperId).size();
    //     long deliveringCount = orderManagementService.getShipperDeliveringOrders(shipperId).size();
    //     long cancelCount = orderManagementService.getShipperCancelCount(shipperId);
    //     model.addAttribute("assignedCount", assignedCount);
    //     model.addAttribute("deliveringCount", deliveringCount);
    //     model.addAttribute("cancelCount", cancelCount);
    //     return "shipper/order/stats";
    // }

    @GetMapping("/{id}/detail")
    public String getOrderDetail(@PathVariable Integer id, 
                                 @RequestParam(required = false) Boolean fromHistory,
                                 Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            OrderDTO order = orderManagementService.getOrderById(id);
            
            // Mark notification as read for this order
            notificationService.markNotificationAsReadByOrder(shipperId, id);
            
            // Nếu xem từ lịch sử hủy, cần hiển thị trạng thái "Shipper hủy" dù đơn đã được giao lại
            if (Boolean.TRUE.equals(fromHistory)) {
                // Lấy thông tin hủy từ lịch sử
                java.util.List<com.example.demo.dto.ShipperCancelHistoryDTO> shipperCancelHistory = 
                    orderManagementService.getShipperCancelHistory(shipperId);
                
                com.example.demo.dto.ShipperCancelHistoryDTO shipperCancel = shipperCancelHistory.stream()
                    .filter(h -> h.getOrderId().equals(id))
                    .findFirst()
                    .orElse(null);
                
                if (shipperCancel != null) {
                    // Ghi đè trạng thái để hiển thị "Shipper hủy"
                    order.setOrderStatus("Shipper_Cancelled");
                    model.addAttribute("viewFromHistory", true);
                }
            }
            
            model.addAttribute("order", order);
            model.addAttribute("shipperName", currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
            return "shipper/order/detail";
        } catch (Exception e) {
            return "redirect:/shipper/orders";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Integer id, 
                              @RequestParam(required = false, defaultValue = "3") Integer shippingTime,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperConfirmOrder(id, shipperId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng và bắt đầu giao! Thời gian giao: 2-5 phút.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper";
    }

    @GetMapping("/{id}/cancel")
    public String showCancelReasonPage(@PathVariable Integer id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            OrderDTO order = orderManagementService.getOrderById(id);
            Long cancelCount = orderManagementService.getShipperCancelCount(shipperId);
            
            model.addAttribute("order", order);
            model.addAttribute("cancelCount", cancelCount);
            return "shipper/order/cancel-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/shipper/orders/" + id + "/detail";
        }
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, 
                             @RequestParam String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperCancelOrder(id, shipperId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper";
    }

    @PostMapping("/{id}/complete")
    @ResponseBody
    public String completeOrder(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "Unauthorized";
        }
        try {
            orderManagementService.markOrderAsDelivered(id);
            return "Success";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/notifications")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> getNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }
        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        return notificationService.getShipperNotifications(shipperId);
    }
}
