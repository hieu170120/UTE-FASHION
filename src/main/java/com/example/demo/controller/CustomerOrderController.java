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
        
        // Lấy tất cả đơn hàng của user (không phân trang trước)
        List<OrderDTO> allOrders = orderManagementService.getCustomerOrders(currentUser.getUserId());
        
        // Sắp xếp theo ngày đặt hàng giảm dần (mới nhất trước)
        allOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        
        // Apply filters nếu có
        List<OrderDTO> filteredOrders = allOrders;
        
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
        
        // Phân trang thủ công sau khi đã filter
        int pageSize = 10;
        int totalElements = filteredOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Đảm bảo page không vượt quá giới hạn
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }
        if (page < 0) {
            page = 0;
        }
        
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        
        List<OrderDTO> pageContent = totalElements > 0 ? 
            filteredOrders.subList(fromIndex, toIndex) : 
            java.util.Collections.emptyList();
        
        // Tạo Page object thủ công
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<OrderDTO> orderPage = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, totalElements
        );
        
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("filteredOrders", null); // Không cần nữa vì đã có trong orderPage
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
            
            // Kiểm tra trạng thái (cho phép hủy khi Processing, Vendor_Confirmed, Confirmed hoặc Shipper_Cancelled)
            if (!"Processing".equals(order.getOrderStatus()) 
                && !"Vendor_Confirmed".equals(order.getOrderStatus())
                && !"Confirmed".equals(order.getOrderStatus())
                && !"Shipper_Cancelled".equals(order.getOrderStatus())) {
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
