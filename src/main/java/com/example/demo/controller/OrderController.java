package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderManagementService orderManagementService;

    @GetMapping
    public String listOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        // Sử dụng method mới với JOIN FETCH để tránh lazy loading
        model.addAttribute("orders", orderManagementService.getCustomerOrders(currentUser.getUserId()));
        return "order/my-orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Integer id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            OrderDTO order = orderService.getOrderById(id);
            if (!order.getUserId().equals(currentUser.getUserId())) {
                return "redirect:/orders";
            }
            model.addAttribute("order", order);
            return "order/detail";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }

    // ❌ REMOVED: createOrder() method
    // Checkout flow is now handled by CheckoutController
    // POST /checkout → CheckoutController.processCheckout()

    @GetMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            orderService.cancelOrder(id, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn hàng!");
        }
        return "redirect:/orders";
    }
}