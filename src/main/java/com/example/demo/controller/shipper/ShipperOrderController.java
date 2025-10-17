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

    @GetMapping
    public String listAssignedOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        List<OrderDTO> assignedOrders = orderManagementService.getShipperAssignedOrders(shipperId);
        List<OrderDTO> deliveringOrders = orderManagementService.getShipperDeliveringOrders(shipperId);
        Long unreadNotificationCount = notificationService.getUnreadNotificationCount(shipperId);
        
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("cancelCount", orderManagementService.getShipperCancelCount(shipperId));
        model.addAttribute("unreadCount", unreadNotificationCount);
        return "shipper/order/list";
    }

    @GetMapping("/stats")
    public String orderStats(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        long assignedCount = orderManagementService.getShipperAssignedOrders(shipperId).size();
        long deliveringCount = orderManagementService.getShipperDeliveringOrders(shipperId).size();
        long cancelCount = orderManagementService.getShipperCancelCount(shipperId);
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("deliveringCount", deliveringCount);
        model.addAttribute("cancelCount", cancelCount);
        return "shipper/order/stats";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperConfirmOrder(id, shipperId);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận giao hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, 
                             @RequestParam(required = false, defaultValue = "Shipper cancel") String reason,
                             HttpSession session, 
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderManagementService.shipperCancelOrder(id, shipperId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy giao hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            orderManagementService.markOrderAsDelivered(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hoàn thành giao hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper/orders";
    }
}