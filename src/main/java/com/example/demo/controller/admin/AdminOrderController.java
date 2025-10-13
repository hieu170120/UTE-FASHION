package com.example.demo.controller.admin;

import com.example.demo.dto.OrderDTO;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(Model model, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<OrderDTO> orderPage = orderService.getAllOrders(PageRequest.of(page, size));
        model.addAttribute("orderPage", orderPage);
        return "admin/order/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Integer id, Model model) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            return "admin/order/detail";
        } catch (Exception e) {
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Integer id, @RequestParam String status, @RequestParam String notes, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status, notes, null); // Admin user ID can be added later
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái đơn hàng!");
        }
        return "redirect:/admin/orders";
    }
}