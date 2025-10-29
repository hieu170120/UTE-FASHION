package com.example.demo.controller.vendor;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.RevenueReportDTO;
import com.example.demo.dto.ShopRegistrationDTO;
import com.example.demo.entity.Shop;
import com.example.demo.service.ExcelExportService;
import com.example.demo.service.OrderManagementService;
import com.example.demo.service.OrderService;
import com.example.demo.service.RevenueReportService;
import com.example.demo.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final RevenueReportService revenueReportService;
    private final ExcelExportService excelExportService;
    private final OrderService orderService;
    private final OrderManagementService orderManagementService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Kiểm tra xem user đã có shop chưa, nếu có thì chuyển hướng
        if (vendorService.getCurrentVendorShop().isPresent()) {
            return "redirect:/vendor/dashboard";
        }
        model.addAttribute("shopRegistrationDTO", new ShopRegistrationDTO());
        return "vendor/registration"; // Path to the registration form view
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("shopRegistrationDTO") ShopRegistrationDTO dto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/registration";
        }

        try {
            vendorService.registerNewShop(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký shop thành công! Vui lòng chờ quản trị viên phê duyệt.");
            return "redirect:/vendor/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/register";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            return "redirect:/vendor/register";
        }
        model.addAttribute("shop", shopOpt.get());
        return "vendor/dashboard"; // Path to the dashboard view
    }

    @GetMapping("/shop/edit")
    public String showEditShopForm(Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có cửa hàng để chỉnh sửa.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopOpt.get();
        ShopRegistrationDTO dto = new ShopRegistrationDTO();
        dto.setShopName(shop.getShopName());
        dto.setDescription(shop.getDescription());
        dto.setLogoUrl(shop.getLogoUrl());

        model.addAttribute("shopDTO", dto);
        return "vendor/shop_edit"; // Path to the shop edit form view
    }

    @PostMapping("/shop/edit")
    public String processEditShop(@Valid @ModelAttribute("shopDTO") ShopRegistrationDTO dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/shop_edit";
        }

        try {
            vendorService.updateCurrentVendorShop(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin shop thành công!");
            return "redirect:/vendor/dashboard";
        } catch (IllegalStateException | jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendor/shop/edit";
        }
    }

    @GetMapping("/report/revenue")
    public String showRevenueReport(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng để xem báo cáo.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopOpt.get();

        // Default to last 7 days if no date range provided
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        RevenueReportDTO report = revenueReportService.getRevenueReport(shop.getId(), start, end);

        model.addAttribute("shop", shop);
        model.addAttribute("report", report);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());
        return "vendor/revenue-report";
    }

    @GetMapping("/report/revenue/export")
    public ResponseEntity<byte[]> exportRevenueReport(@RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate,
                                                       RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Shop shop = shopOpt.get();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        try {
            RevenueReportDTO report = revenueReportService.getRevenueReport(shop.getId(), start, end);
            byte[] excelData = excelExportService.exportRevenueReport(report, start.toString(), end.toString());

            String filename = "BaoCaoDoanhThu_" + shop.getShopName() + "_" + start + "_" + end + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/order-status")
    public String showOrderStatus(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String fromDate,
                                  @RequestParam(required = false) String toDate,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng để xem đơn hàng.");
            return "redirect:/vendor/register";
        }
        Shop shop = shopOpt.get();

        // Parse date từ string (nếu có)
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            fromDateTime = LocalDate.parse(fromDate).atStartOfDay();  // Bắt đầu ngày
        }
        if (toDate != null && !toDate.isEmpty()) {
            toDateTime = LocalDate.parse(toDate).atTime(23, 59, 59);  // Cuối ngày
        }

        // Thêm sort DESC theo orderDate (mới nhất đầu tiên)
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        // Gọi service với filters
        Page<OrderDTO> orderPage = orderService.getOrdersByShopIdWithFilters(shop.getId(), status, fromDateTime, toDateTime, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page + 1);  // Hiển thị page 1-based trong HTML
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);  // Giữ nguyên string để bind lại form
        model.addAttribute("toDate", toDate);      // Giữ nguyên string để bind lại form

        return "vendor/order-status";
    }
    
    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(@PathVariable Integer orderId, Model model) {
        Optional<OrderDTO> orderDTO = orderService.getOrderDetails(orderId);
        if (orderDTO.isPresent()) {
            model.addAttribute("order", orderDTO.get());
        } else {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng.");
        }
        return "vendor/order-status-detail";
    }
    
    /**
     * Vendor xác nhận có hàng
     */
    @PostMapping("/orders/{orderId}/confirm")
    public String confirmOrder(@PathVariable Integer orderId, 
                              RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng để xác nhận đơn hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            orderManagementService.vendorConfirmOrder(orderId, shopOpt.get().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận đơn hàng thành công! Đơn hàng đang chờ admin chọn shipper.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/orders/" + orderId;
    }
    
    /**
     * Hiển thị trang nhập lý do từ chối
     */
    @GetMapping("/orders/{orderId}/reject")
    public String showRejectForm(@PathVariable Integer orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        Optional<OrderDTO> orderDTO = orderService.getOrderDetails(orderId);
        if (orderDTO.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/vendor/order-status";
        }
        
        model.addAttribute("order", orderDTO.get());
        return "vendor/order-reject-reason";
    }
    
    /**
     * Xử lý từ chối đơn hàng
     */
    @PostMapping("/orders/{orderId}/reject")
    public String rejectOrder(@PathVariable Integer orderId,
                             @RequestParam String reason,
                             RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng để từ chối đơn hàng.");
            return "redirect:/vendor/register";
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối.");
            return "redirect:/vendor/orders/" + orderId + "/reject";
        }
        
        try {
            orderManagementService.vendorRejectOrder(orderId, shopOpt.get().getId(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/order-status";
    }
    
    // ===== XỚ LÝ TRẢ HÀNG =====
    
    /**
     * Danh sách yêu cầu trả hàng
     */
    @GetMapping("/orders/returns")
    public String returnRequests(Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        List<com.example.demo.dto.OrderReturnRequestDTO> returnRequests = 
                orderManagementService.getPendingReturnRequestsByShop(shopOpt.get().getId());
        model.addAttribute("returnRequests", returnRequests);
        return "vendor/order/returns";
    }
    
    /**
     * Phê duyệt trả hàng
     */
    @PostMapping("/orders/returns/{id}/approve")
    public String approveReturn(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            orderManagementService.vendorApproveReturnRequest(id, shopOpt.get().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp nhận trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/orders/returns";
    }
    
    /**
     * Hiển thị trang nhập lý do từ chối
     */
    @GetMapping("/orders/returns/{id}/reject")
    public String showRejectReturnReasonPage(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            // Lấy thông tin return request
            List<com.example.demo.dto.OrderReturnRequestDTO> allRequests = 
                    orderManagementService.getPendingReturnRequestsByShop(shopOpt.get().getId());
            com.example.demo.dto.OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Không tìm thấy yêu cầu"));
            
            model.addAttribute("returnRequest", returnRequest);
            return "vendor/order/reject-return-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/vendor/orders/returns";
        }
    }
    
    /**
     * Xử lý từ chối trả hàng
     */
    @PostMapping("/orders/returns/{id}/reject")
    public String rejectReturn(@PathVariable Integer id, 
                              @RequestParam String rejectionReason,
                              RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            orderManagementService.vendorRejectReturnRequest(id, shopOpt.get().getId(), rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/orders/returns";
    }
    
    // ===== XỚ LÝ TRẢ HÀNG TỪ ORDER DETAIL PAGE =====
    
    /**
     * Chấp nhận trả hàng từ order detail page
     */
    @PostMapping("/orders/{orderId}/returns/approve")
    public String approveReturnFromDetail(@PathVariable Integer orderId, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            // Tìm return request theo orderId
            List<com.example.demo.dto.OrderReturnRequestDTO> allRequests = 
                    orderManagementService.getPendingReturnRequestsByShop(shopOpt.get().getId());
            com.example.demo.dto.OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            orderManagementService.vendorApproveReturnRequest(returnRequest.getId(), shopOpt.get().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp nhận trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/orders/" + orderId;
    }
    
    /**
     * Hiển thị trang từ chối trả hàng từ order detail page
     */
    @GetMapping("/orders/{orderId}/returns/reject")
    public String showRejectReturnReasonFromDetail(@PathVariable Integer orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            // Lấy thông tin return request theo orderId
            List<com.example.demo.dto.OrderReturnRequestDTO> allRequests = 
                    orderManagementService.getPendingReturnRequestsByShop(shopOpt.get().getId());
            com.example.demo.dto.OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            model.addAttribute("returnRequest", returnRequest);
            model.addAttribute("fromDetailPage", true); // Flag để biết redirect về đâu
            return "vendor/order/reject-return-reason";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/vendor/orders/" + orderId;
        }
    }
    
    /**
     * Xử lý từ chối trả hàng từ order detail page
     */
    @PostMapping("/orders/{orderId}/returns/reject")
    public String rejectReturnFromDetail(@PathVariable Integer orderId,
                                         @RequestParam String rejectionReason,
                                         RedirectAttributes redirectAttributes) {
        Optional<Shop> shopOpt = vendorService.getCurrentVendorShop();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần có cửa hàng.");
            return "redirect:/vendor/register";
        }
        
        try {
            // Tìm return request theo orderId
            List<com.example.demo.dto.OrderReturnRequestDTO> allRequests = 
                    orderManagementService.getPendingReturnRequestsByShop(shopOpt.get().getId());
            com.example.demo.dto.OrderReturnRequestDTO returnRequest = allRequests.stream()
                .filter(req -> req.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Không tìm thấy yêu cầu trả hàng"));
            
            orderManagementService.vendorRejectReturnRequest(returnRequest.getId(), shopOpt.get().getId(), rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu trả hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/vendor/orders/" + orderId;
    }
}
