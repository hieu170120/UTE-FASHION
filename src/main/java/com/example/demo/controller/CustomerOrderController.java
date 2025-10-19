package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Trang lịch sử đơn hàng của khách hàng (có phân trang)
     */
    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String fromDate,
                          @RequestParam(required = false) String toDate,
                          HttpSession session, 
                          Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Pageable with sorting by orderDate DESC (newest first)
        Pageable pageable = PageRequest.of(page, 10);
        Page<OrderDTO> orderPage = orderService.getUserOrdersPageable(currentUser.getUserId(), pageable);
        
        // Apply filters if provided
        List<OrderDTO> filteredOrders = orderPage.getContent();
        
        if (status != null && !status.isEmpty()) {
            filteredOrders = filteredOrders.stream()
                .filter(order -> order.getOrderStatus().equals(status))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (fromDate != null && !fromDate.isEmpty()) {
            java.time.LocalDate from = java.time.LocalDate.parse(fromDate);
            filteredOrders = filteredOrders.stream()
                .filter(order -> !order.getOrderDate().toLocalDate().isBefore(from))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (toDate != null && !toDate.isEmpty()) {
            java.time.LocalDate to = java.time.LocalDate.parse(toDate);
            filteredOrders = filteredOrders.stream()
                .filter(order -> !order.getOrderDate().toLocalDate().isAfter(to))
                .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("filteredOrders", filteredOrders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
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
     * Hiển thị trang nhập lý do hủy đơn
     */
    @GetMapping("/{orderId}/cancel")
    public String showCancelReasonPage(@PathVariable Integer orderId,
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
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng này");
                return "redirect:/orders/my-orders";
            }
            
            // Kiểm tra trạng thái
            if (!"Processing".equals(order.getOrderStatus()) && !"Confirmed".equals(order.getOrderStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn ở trạng thái này");
                return "redirect:/orders/" + orderId + "/detail";
            }
            
            model.addAttribute("order", order);
            return "order/cancel-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng");
            return "redirect:/orders/my-orders";
        }
    }
    
    /**
     * Xử lý hủy đơn hàng
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId,
                             @RequestParam(name = "cancelReason") String cancelReason,  
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Vui lòng nhập lý do hủy đơn");
            return "redirect:/orders/" + orderId + "/detail";
        }
        
        try {
            orderManagementService.customerCancelOrder(orderId, currentUser.getUserId(), cancelReason);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã hủy đơn hàng thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Lỗi khi hủy đơn: " + e.getMessage());
        }
        
        // Redirect back to detail page
        return "redirect:/orders/" + orderId + "/detail";
    }

    /**
     * Hiển thị trang nhập lý do trả hàng
     */
    @GetMapping("/{orderId}/request-return")
    public String showReturnReasonPage(@PathVariable Integer orderId,
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
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền yêu cầu trả hàng đơn này");
                return "redirect:/orders/my-orders";
            }
            
            // Kiểm tra trạng thái
            if (!"Delivered".equals(order.getOrderStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể trả hàng khi đơn đã được giao");
                return "redirect:/orders/" + orderId + "/detail";
            }
            
            model.addAttribute("order", order);
            return "order/return-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng");
            return "redirect:/orders/my-orders";
        }
    }
    
    /**
     * Xử lý yêu cầu trả hàng
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
        
        // Redirect back to detail page
        return "redirect:/orders/" + orderId + "/detail";
    }
}
