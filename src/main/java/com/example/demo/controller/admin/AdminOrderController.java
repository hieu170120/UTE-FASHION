package com.example.demo.controller.admin;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderReturnRequestDTO;
import com.example.demo.dto.ShipperCancelHistoryDTO;
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
        // Sort by orderDate DESC (newest first) and filter in database
        Page<OrderDTO> orderPage = orderService.findOrdersByFilters(
            status, fromDate, toDate,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"))
        );
        
        model.addAttribute("orderPage", orderPage);
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
            
            // Luôn load lịch sử hủy để admin xem (dù đơn đã được giao lại)
            List<ShipperCancelHistoryDTO> cancelHistory = orderManagementService.getOrderCancelHistory(id);
            model.addAttribute("cancelHistory", cancelHistory);
            
            // Nếu đơn đã được vendor xác nhận hoặc shipper hủy, load danh sách shipper đang hoạt động theo carrier
            if (("Vendor_Confirmed".equals(order.getOrderStatus()) || "Shipper_Cancelled".equals(order.getOrderStatus())) 
                && order.getCarrierId() != null) {
                List<ShipperDTO> shippers = shipperService.getActiveShippersByCarrier(order.getCarrierId());
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
    
}
