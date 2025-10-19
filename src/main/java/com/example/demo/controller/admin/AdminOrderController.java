package com.example.demo.controller.admin;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderReturnRequestDTO;
import com.example.demo.dto.ShipperDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CarrierService;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ShipperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderManagementService orderManagementService;
    
    @Autowired
    private ShipperService shipperService;
    
    @Autowired
    private CarrierService carrierService;

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "AdminOrderController is working!";
    }

    @GetMapping
    public String listOrders(Model model,
                           @RequestParam(name = "page", defaultValue = "0") int page, 
                           @RequestParam(name = "size", defaultValue = "10") int size,
                           @RequestParam(name = "status", required = false) String status,
                           @RequestParam(name = "fromDate", required = false) String fromDate,
                           @RequestParam(name = "toDate", required = false) String toDate) {
        // Sort by orderDate DESC (newest first)
        Page<OrderDTO> orderPage = orderService.getAllOrders(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"))
        );
        
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
        return "admin/order/list";
    }
    
    // Đơn hàng chờ xử lý
    @GetMapping("/pending")
    public String pendingOrders(Model model) {
        List<OrderDTO> pendingOrders = orderManagementService.getPendingOrders();
        model.addAttribute("pendingOrders", pendingOrders);
        return "admin/order/pending-list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Integer id, Model model) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            
            // Nếu đơn đang chờ xử lý, load danh sách shipper theo carrier
            if ("Processing".equals(order.getOrderStatus()) && order.getCarrierId() != null) {
                List<ShipperDTO> shippers = shipperService.getShippersByCarrier(order.getCarrierId());
                model.addAttribute("shippers", shippers);
            }
            
            return "admin/order/detail";
        } catch (Exception e) {
            return "redirect:/admin/orders";
        }
    }

    // DISABLED: Admin không được phép cập nhật trạng thái thủ công
    // Trạng thái đơn hàng chỉ được cập nhật tự động qua quy trình của shipper
    /*
    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Integer id, 
                                   @RequestParam String status, 
                                   @RequestParam(required = false, defaultValue = "") String notes, 
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status, notes, null);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    */
    
    // DISABLED: Admin không được phép cập nhật trạng thái thủ công
    // Quick update - chuyển trạng thái nhanh từ danh sách
    /*
    @PostMapping("/{id}/quick-update")
    public String quickUpdateStatus(@PathVariable Integer id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            String note = "Admin chuyển trạng thái nhanh từ danh sách";
            orderService.updateOrderStatus(id, status, note, null);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    */
    
    // Admin xác nhận đơn và chọn shipper
    @PostMapping("/{id}/assign-shipper")
    public String assignShipper(@PathVariable Integer id,
                               @RequestParam Integer shipperId,
                               RedirectAttributes redirectAttributes) {
        try {
            orderManagementService.adminConfirmOrderAndAssignShipper(id, shipperId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn và giao cho shipper!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/pending";
    }
    
    // Yêu cầu trả hàng
    @GetMapping("/returns")
    public String returnRequests(Model model) {
        List<OrderReturnRequestDTO> returnRequests = orderManagementService.getPendingReturnRequests();
        model.addAttribute("returnRequests", returnRequests);
        return "admin/order/returns";
    }
    
    // Phê duyệt trả hàng
    @PostMapping("/returns/{id}/approve")
    public String approveReturn(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderManagementService.approveReturnRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp nhận trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/returns";
    }
    
    // Hiển thị trang nhập lý do từ chối
    @GetMapping("/returns/{id}/reject")
    public String showRejectReturnReasonPage(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin return request
            java.util.List<OrderReturnRequestDTO> allRequests = orderManagementService.getPendingReturnRequests();
            OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu"));
            
            model.addAttribute("returnRequest", returnRequest);
            return "admin/order/reject-return-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders/returns";
        }
    }
    
    // Xử lý từ chối trả hàng
    @PostMapping("/returns/{id}/reject")
    public String rejectReturn(@PathVariable Integer id, 
                              @RequestParam String rejectionReason,
                              RedirectAttributes redirectAttributes) {
        try {
            orderManagementService.rejectReturnRequest(id, rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/returns";
    }
    
    // ===== APPROVE/REJECT TỪ ORDER DETAIL PAGE =====
    
    // Approve return từ detail page
    @PostMapping("/{orderId}/returns/approve")
    public String approveReturnFromDetail(@PathVariable Integer orderId, RedirectAttributes redirectAttributes) {
        try {
            // Tìm return request theo orderId
            java.util.List<OrderReturnRequestDTO> allRequests = orderManagementService.getPendingReturnRequests();
            OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            orderManagementService.approveReturnRequest(returnRequest.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp nhận trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
    
    // Hiển thị trang từ chối từ detail page
    @GetMapping("/{orderId}/returns/reject")
    public String showRejectReturnReasonFromDetail(@PathVariable Integer orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin return request theo orderId
            java.util.List<OrderReturnRequestDTO> allRequests = orderManagementService.getPendingReturnRequests();
            OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            model.addAttribute("returnRequest", returnRequest);
            model.addAttribute("fromDetailPage", true); // Flag để biết redirect về đâu
            return "admin/order/reject-return-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders/" + orderId;
        }
    }
    
    // Xử lý từ chối từ detail page
    @PostMapping("/{orderId}/returns/reject")
    public String rejectReturnFromDetail(@PathVariable Integer orderId,
                                         @RequestParam String rejectionReason,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Tìm return request theo orderId
            java.util.List<OrderReturnRequestDTO> allRequests = orderManagementService.getPendingReturnRequests();
            OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            orderManagementService.rejectReturnRequest(returnRequest.getId(), rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
}
