package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class CustomerOrderController {

    @Autowired
    private OrderManagementService orderManagementService;
    
    @Autowired
    private OrderService orderService;

    /**
     * Trang lịch sử đơn hàng của khách hàng
     */
    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<OrderDTO> orders = orderManagementService.getCustomerOrders(currentUser.getUserId());
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Đơn Hàng Của Tôi");
        
        return "order/my-orders";
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/{orderId}/detail")
    public String orderDetail(@PathVariable Integer orderId, 
                             HttpSession session, 
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            
            // Kiểm tra ownership
            if (!order.getUserId().equals(currentUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này");
                return "redirect:/orders/my-orders";
            }
            
            model.addAttribute("order", order);
            return "order/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng");
            return "redirect:/orders/my-orders";
        }
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            orderManagementService.customerCancelOrder(orderId, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã hủy đơn hàng thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Lỗi khi hủy đơn: " + e.getMessage());
        }
        
        return "redirect:/orders/my-orders";
    }

    /**
     * Yêu cầu trả hàng
     */
    @PostMapping("/{orderId}/request-return")
    public String requestReturn(@PathVariable Integer orderId,
                               @RequestParam String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Vui lòng nhập lý do trả hàng");
            return "redirect:/orders/my-orders";
        }
        
        try {
            orderManagementService.customerRequestReturn(orderId, currentUser.getUserId(), reason);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã gửi yêu cầu trả hàng. Chúng tôi sẽ xem xét và phản hồi sớm!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/orders/my-orders";
    }
}
