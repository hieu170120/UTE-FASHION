package com.example.demo.controller.shipper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.OrderService;
import com.example.demo.service.ShipperService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shipper/orders")
public class ShipperOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipperService shipperService;

    @GetMapping
    public String listAssignedOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        List<OrderDTO> orders = orderService.getOrdersByShipper(shipperId);
        model.addAttribute("orders", orders);
        return "shipper/order/list";
    }

    @GetMapping("/stats")
    public String orderStats(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
        model.addAttribute("stats", orderService.getShipperOrderStats(shipperId));
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
            orderService.shipperConfirmOrder(id, shipperId);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận giao hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xác nhận giao hàng!");
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().stream().noneMatch(role -> "SHIPPER".equals(role.getRoleName()))) {
            return "redirect:/login";
        }
        try {
            Integer shipperId = shipperService.getShipperIdByUserId(currentUser.getUserId());
            orderService.shipperCancelOrder(id, shipperId);
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
            orderService.completeDelivery(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hoàn thành giao hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hoàn thành giao hàng!");
        }
        return "redirect:/shipper/orders";
    }
}