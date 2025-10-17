// UPDATED: AdminOrderController.java - Cập nhật để hỗ trợ quy trình mới

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ShipperService shipperService;
    
    @Autowired
    private CarrierService carrierService;

    /**
     * Danh sách đơn hàng với filter theo status
     */
    @GetMapping
    public String listOrders(Model model, 
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size,
                            @RequestParam(name = "status", required = false) String status) {
        
        Page<OrderDTO> orderPage;
        if (status != null && !status.isEmpty()) {
            // Filter by status
            List<OrderDTO> orders = orderService.getOrdersByStatus(status);
            orderPage = new PageImpl<>(orders, PageRequest.of(page, size), orders.size());
        } else {
            orderPage = orderService.getAllOrders(PageRequest.of(page, size));
        }
        
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", Arrays.asList(
            "Processing", "Confirmed", "Shipping", "Delivered", "Cancelled", "RETURN_REQUESTED"
        ));
        
        return "admin/order/list";
    }

    /**
     * Chi tiết đơn hàng với form chọn shipper
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Integer id, Model model) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            
            // Nếu đơn hàng Processing, hiển thị list shipper để chọn
            if ("Processing".equals(order.getOrderStatus()) && order.getCarrierId() != null) {
                List<ShipperDTO> availableShippers = orderService.getAvailableShippersByCarrier(order.getCarrierId());
                model.addAttribute("availableShippers", availableShippers);
            }
            
            return "admin/order/detail";
        } catch (Exception e) {
            return "redirect:/admin/orders";
        }
    }

    /**
     * ADMIN: Xác nhận đơn hàng và chọn shipper
     * Processing → Confirmed
     */
    @PostMapping("/{id}/assign-shipper")
    public String assignShipperAndConfirm(@PathVariable Integer id,
                                         @RequestParam Integer shipperId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Lấy admin ID từ session
            User currentAdmin = (User) session.getAttribute("currentUser");
            Integer adminId = currentAdmin != null ? currentAdmin.getUserId() : null;
            
            orderService.adminConfirmOrderWithShipper(id, shipperId, adminId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã xác nhận đơn hàng và giao cho shipper!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }

    /**
     * ADMIN: Xử lý yêu cầu trả hàng
     */
    @PostMapping("/{id}/process-return")
    public String processReturnRequest(@PathVariable Integer id,
                                     @RequestParam boolean approved,
                                     @RequestParam String notes,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = (User) session.getAttribute("currentUser");
            Integer adminId = currentAdmin != null ? currentAdmin.getUserId() : null;
            
            orderService.processReturnRequest(id, adminId, approved, notes);
            
            String message = approved ? "Đã chấp thuận yêu cầu trả hàng" : "Đã từ chối yêu cầu trả hàng";
            redirectAttributes.addFlashAttribute("successMessage", message);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Danh sách yêu cầu trả hàng
     */
    @GetMapping("/returns")
    public String listReturnRequests(Model model) {
        List<OrderDTO> returnRequests = orderService.getOrdersByStatus("RETURN_REQUESTED");
        model.addAttribute("returnRequests", returnRequests);
        return "admin/order/returns";
    }

    /**
     * ADMIN: Cập nhật trạng thái đơn hàng manual (fallback)
     */
    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Integer id, 
                                  @RequestParam String status, 
                                  @RequestParam(required = false) String notes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = (User) session.getAttribute("currentUser");
            Integer adminId = currentAdmin != null ? currentAdmin.getUserId() : null;
            
            orderService.updateOrderStatus(id, status, notes, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }

    /**
     * API: Lấy shipper theo carrier (AJAX)
     */
    @GetMapping("/api/shippers-by-carrier/{carrierId}")
    @ResponseBody
    public ResponseEntity<List<ShipperDTO>> getShippersByCarrier(@PathVariable Integer carrierId) {
        try {
            List<ShipperDTO> shippers = orderService.getAvailableShippersByCarrier(carrierId);
            return ResponseEntity.ok(shippers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
}