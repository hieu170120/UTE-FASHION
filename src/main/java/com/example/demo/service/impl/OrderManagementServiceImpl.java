package com.example.demo.service.impl;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderReturnRequestDTO;
import com.example.demo.dto.ShipperCancelHistoryDTO;
import com.example.demo.entity.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.DailyAnalyticsService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.OrderManagementService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderManagementServiceImpl implements OrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ShipperRepository shipperRepository;
    
    @Autowired
    private OrderReturnRequestRepository returnRequestRepository;
    
    @Autowired
    private ShipperCancelHistoryRepository cancelHistoryRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DailyAnalyticsService dailyAnalyticsService;
    
    private static final int MAX_SHIPPER_CANCEL_COUNT = 3;
    private static final Random random = new Random();

    // === VENDOR FUNCTIONS ===

    @Override
    @Transactional
    public void vendorConfirmOrder(Integer orderId, Integer shopId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        
        // Kiểm tra đơn hàng có thuộc shop này không
        if (order.getShop() == null || !order.getShop().getId().equals(shopId)) {
            throw new IllegalStateException("Bạn không có quyền xử lý đơn hàng này");
        }
        
        // Kiểm tra trạng thái - chỉ cho phép xác nhận khi đang Processing
        if (!OrderStatus.PROCESSING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xác nhận");
        }
        
        // Cập nhật trạng thái sang Vendor_Confirmed
        order.setOrderStatus(OrderStatus.VENDOR_CONFIRMED.getValue());
        order.setConfirmedAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        logger.info("✅ Vendor (Shop ID: {}) đã xác nhận đơn hàng #{}", shopId, order.getOrderNumber());
    }

    @Override
    @Transactional
    public void vendorRejectOrder(Integer orderId, Integer shopId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        
        // Kiểm tra đơn hàng có thuộc shop này không
        if (order.getShop() == null || !order.getShop().getId().equals(shopId)) {
            throw new IllegalStateException("Bạn không có quyền xử lý đơn hàng này");
        }
        
        // Kiểm tra trạng thái - chỉ cho phép từ chối khi đang Processing
        if (!OrderStatus.PROCESSING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xác nhận");
        }
        
        // Cập nhật trạng thái sang Cancelled và lưu lý do
        order.setOrderStatus(OrderStatus.CANCELLED.getValue());
        order.setCancelReason("Vendor từ chối: " + reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy("VENDOR");
        
        orderRepository.save(order);
        
        logger.info("❌ Vendor (Shop ID: {}) đã từ chối đơn hàng #{} - Lý do: {}", shopId, order.getOrderNumber(), reason);
    }

    // === ADMIN FUNCTIONS ===

    @Override
    public List<OrderDTO> getPendingOrders() {
        // Admin chỉ thấy đơn đã được vendor xác nhận (Vendor_Confirmed)
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.VENDOR_CONFIRMED.getValue());
        return orders.stream()
                .map(this::mapToOrderDTOWithImages)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void adminConfirmOrderAndAssignShipper(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shipper"));
        
        // Kiểm tra trạng thái đơn hàng (cho phép Vendor_Confirmed hoặc Shipper_Cancelled)
        if (!OrderStatus.VENDOR_CONFIRMED.getValue().equals(order.getOrderStatus()) 
            && !OrderStatus.SHIPPER_CANCELLED.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xử lý (vendor đã xác nhận) hoặc shipper hủy");
        }
        
        // KHÔNG xóa thông tin hủy cũ - giữ lại để admin xem lịch sử
        // Shipper mới sẽ không thấy vì chỉ hiển thị khi orderStatus = Shipper_Cancelled
        
        // Gán shipper và cập nhật trạng thái
        order.setShipper(shipper);
        order.setOrderStatus(OrderStatus.CONFIRMED.getValue());
        order.setConfirmedAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        // Gửi thông báo cho shipper
        notificationService.notifyShipperNewOrder(shipper, order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderReturnRequestDTO> getPendingReturnRequests() {
        List<OrderReturnRequest> requests = returnRequestRepository.findByStatus("Pending");
        return requests.stream()
                .map(req -> {
                    OrderReturnRequestDTO dto = new OrderReturnRequestDTO();
                    dto.setId(req.getId());
                    dto.setOrderId(req.getOrder().getId());
                    dto.setOrderCode(req.getOrder().getOrderNumber());
                    dto.setUserId(req.getUser().getUserId());
                    dto.setUserName(req.getUser().getFullName());
                    dto.setUserEmail(req.getUser().getEmail());
                    dto.setReason(req.getReason());
                    dto.setStatus(req.getStatus());
                    dto.setRequestDate(req.getRequestedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveReturnRequest(Integer requestId) {
        OrderReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu"));
        
        request.setStatus("Approved");
        request.setApprovedAt(LocalDateTime.now());
        
        // Cập nhật trạng thái đơn hàng
        Order order = request.getOrder();
        
        // ✅ HOÀN XU nếu đã thanh toán bằng QR hoặc COIN
        refundCoinsIfEligible(order);
        
        order.setOrderStatus(OrderStatus.RETURNED.getValue());
        
        returnRequestRepository.save(request);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void rejectReturnRequest(Integer requestId, String rejectionReason) {
        OrderReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu"));
        
        request.setStatus("Rejected");
        request.setRejectedAt(LocalDateTime.now());
        
        // Khôi phục trạng thái đơn hàng về Đã giao và lưu lý do từ chối
        Order order = request.getOrder();
        order.setOrderStatus(OrderStatus.DELIVERED.getValue());
        order.setAdminNotes("Lý do từ chối trả hàng: " + rejectionReason);
        
        returnRequestRepository.save(request);
        orderRepository.save(order);
    }

    // === SHIPPER FUNCTIONS ===

    @Override
    @Transactional
    public void shipperConfirmOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra shipper có đúng là người được giao không
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new IllegalStateException("Bạn không có quyền xác nhận đơn này");
        }
        
        // Kiểm tra trạng thái
        if (!OrderStatus.CONFIRMED.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ giao");
        }
        
        // Cập nhật trạng thái và thời gian
        order.setOrderStatus(OrderStatus.SHIPPING.getValue());
        order.setAcceptedAt(LocalDateTime.now());
        order.setShippedAt(LocalDateTime.now());
        
        // Random thời gian giao: 2-5 phút
        int randomMinutes = 2 + random.nextInt(4); // 2, 3, 4, hoặc 5
        order.setShippingTime(randomMinutes);
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(randomMinutes));
        
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void shipperCancelOrder(Integer orderId, Integer shipperId, String reason) {
        // Kiểm tra số lần hủy
        Long cancelCount = cancelHistoryRepository.countByShipperId(shipperId);
        if (cancelCount >= MAX_SHIPPER_CANCEL_COUNT) {
            throw new IllegalStateException("Bạn đã hủy " + MAX_SHIPPER_CANCEL_COUNT + 
                    " đơn. Không thể hủy thêm!");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shipper"));
        
        // Kiểm tra quyền
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn này");
        }
        
        // Lưu lịch sử hủy
        ShipperCancelHistory history = new ShipperCancelHistory();
        history.setShipper(shipper);
        history.setOrder(order);
        history.setReason(reason);
        cancelHistoryRepository.save(history);
        
        // Tăng số lần hủy của shipper
        shipper.setCancelCount(shipper.getCancelCount() + 1);
        shipperRepository.save(shipper);
        
        // Chuyển sang trạng thái SHIPPER_CANCELLED (chỉ hiện với admin và shipper)
        order.setOrderStatus(OrderStatus.SHIPPER_CANCELLED.getValue());
        order.setShipper(null);
        order.setConfirmedAt(null);
        order.setCancelledBy("SHIPPER");
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        
        orderRepository.save(order);
    }

    @Override
    public Long getShipperCancelCount(Integer shipperId) {
        return cancelHistoryRepository.countByShipperId(shipperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShipperCancelHistoryDTO> getShipperCancelHistory(Integer shipperId) {
        List<ShipperCancelHistory> history = cancelHistoryRepository.findByShipperIdOrderByCancelledAtDesc(shipperId);
        return history.stream()
                .map(h -> {
                    ShipperCancelHistoryDTO dto = new ShipperCancelHistoryDTO();
                    dto.setId(h.getId());
                    dto.setOrderId(h.getOrder().getId());
                    dto.setOrderNumber(h.getOrder().getOrderNumber());
                    dto.setShipperName(h.getShipper().getFullName());
                    dto.setReason(h.getReason());
                    dto.setCancelledAt(h.getCancelledAt());
                    dto.setOrderStatus(h.getOrder().getOrderStatus());
                    dto.setRecipientName(h.getOrder().getRecipientName());
                    dto.setTotalAmount(h.getOrder().getTotalAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShipperCancelHistoryDTO> getOrderCancelHistory(Integer orderId) {
        List<ShipperCancelHistory> history = cancelHistoryRepository.findByOrderIdOrderByCancelledAtDesc(orderId);
        return history.stream()
                .map(h -> {
                    ShipperCancelHistoryDTO dto = new ShipperCancelHistoryDTO();
                    dto.setId(h.getId());
                    dto.setOrderId(h.getOrder().getId());
                    dto.setOrderNumber(h.getOrder().getOrderNumber());
                    dto.setShipperName(h.getShipper().getFullName());
                    dto.setReason(h.getReason());
                    dto.setCancelledAt(h.getCancelledAt());
                    dto.setOrderStatus(h.getOrder().getOrderStatus());
                    dto.setRecipientName(h.getOrder().getRecipientName());
                    dto.setTotalAmount(h.getOrder().getTotalAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void markOrderAsDelivered(Integer orderId) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔔 [SHIPPER DELIVERY] Marking order as delivered - OrderID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        logger.info("   Shop: {}, TotalAmount: {}", 
            order.getShop() != null ? order.getShop().getShopName() : "Unknown",
            order.getTotalAmount());
        
        order.setOrderStatus(OrderStatus.DELIVERED.getValue());
        order.setDeliveredAt(LocalDateTime.now());
        
        orderRepository.save(order);
        logger.info("✅ Order status saved to database");
        
        // 🔥 TRIGGER COMMISSION CALCULATION
        logger.info("📍 Calling updateDailyAnalyticsForOrder for commission calculation...");
        try {
            dailyAnalyticsService.updateDailyAnalyticsForOrder(order);
            logger.info("✅ Commission calculation completed successfully");
        } catch (RuntimeException e) {
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("❌ [SHIPPER DELIVERY] COMMISSION CALCULATION FAILED");
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("🔴 Error: {}", e.getMessage());
            logger.error("🔴 Cause: {}", e.getCause());
            logger.error("═════════════════════════════════════════════════════════════");
            
            // ⚠️ Re-throw error so it's visible to the caller
            throw e;
        } catch (Exception e) {
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("❌ [SHIPPER DELIVERY] UNEXPECTED ERROR IN COMMISSION CALCULATION");
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("🔴 Exception Type: {}", e.getClass().getName());
            logger.error("🔴 Exception Message: {}", e.getMessage());
            logger.error("", e);
            logger.error("═════════════════════════════════════════════════════════════");
            
            throw new RuntimeException("Lỗi tính chiết khấu - " + e.getMessage(), e);
        }
        logger.info("═══════════════════════════════════════════════════════════");
    }
    
    @Override
    @Transactional
    public void shipperConfirmCODPayment(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra shipper có đúng là người được giao không
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new IllegalStateException("Bạn không có quyền xác nhận đơn này");
        }
        
        // Kiểm tra trạng thái
        if (!OrderStatus.SHIPPING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái đang giao");
        }
        
        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(OrderStatus.DELIVERED.getValue());
        order.setDeliveredAt(LocalDateTime.now());
        order.setPaymentStatus("Paid");
        
        orderRepository.save(order);
        
        // Cập nhật trạng thái thanh toán trong bảng Payment (nếu là COD)
        paymentRepository.findByOrderIdWithPaymentMethod(orderId).ifPresent(payment -> {
            if ("COD".equalsIgnoreCase(payment.getPaymentMethod().getMethodCode())) {
                payment.setPaymentStatus("Success");
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        });
        
        // 🔥 TRIGGER COMMISSION CALCULATION
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔔 [COD PAYMENT CONFIRMED] Calculating commission - OrderID: {}", orderId);
        logger.info("   Shop: {}, TotalAmount: {}", 
            order.getShop() != null ? order.getShop().getShopName() : "Unknown",
            order.getTotalAmount());
        
        try {
            logger.info("📍 Calling updateDailyAnalyticsForOrder for commission calculation...");
            dailyAnalyticsService.updateDailyAnalyticsForOrder(order);
            logger.info("✅ Commission calculation completed successfully");
            logger.info("═══════════════════════════════════════════════════════════");
        } catch (RuntimeException e) {
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("❌ [COD PAYMENT COMMISSION] CALCULATION FAILED");
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("🔴 Error: {}", e.getMessage());
            logger.error("🔴 Cause: {}", e.getCause());
            logger.error("═════════════════════════════════════════════════════════════");
            
            // ⚠️ Re-throw error so it's visible to the caller
            throw e;
        } catch (Exception e) {
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("❌ [COD PAYMENT COMMISSION] UNEXPECTED ERROR");
            logger.error("═════════════════════════════════════════════════════════════");
            logger.error("🔴 Exception Type: {}", e.getClass().getName());
            logger.error("🔴 Exception: {}", e.getMessage());
            logger.error("", e);
            logger.error("═════════════════════════════════════════════════════════════");
            
            throw new RuntimeException("Lỗi tính chiết khấu COD payment - " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void shipperMarkAsFailedDelivery(Integer orderId, Integer shipperId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shipper"));
        
        // Kiểm tra quyền
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new IllegalStateException("Bạn không có quyền xử lý đơn này");
        }
        
        // Kiểm tra trạng thái
        if (!OrderStatus.SHIPPING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái đang giao");
        }
        
        ShipperCancelHistory history = new ShipperCancelHistory();
        history.setShipper(shipper);
        history.setOrder(order);
        history.setReason("Không giao được:" + reason);
        cancelHistoryRepository.save(history);
        
        // Chuyển về trạng thái PROCESSING để admin xử lý
        order.setOrderStatus(OrderStatus.PROCESSING.getValue());
        order.setShipper(null);
        order.setConfirmedAt(null);
        order.setAcceptedAt(null);
        order.setShippedAt(null);
        order.setEstimatedDeliveryTime(null);
        order.setShippingTime(null);
        order.setCancelledBy("Shipper");
        order.setCancelReason("Không giao được: " + reason);
        order.setCancelledAt(LocalDateTime.now());
        
        orderRepository.save(order);
    }

    // === CUSTOMER FUNCTIONS ===

    @Override
    @Transactional
    public void customerCancelOrder(Integer orderId, Integer userId, String cancelReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra quyền sở hữu
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn này");
        }
        
        // Chỉ cho phép hủy khi đơn đang xử lý, vendor đã xác nhận, đã xử lý hoặc shipper hủy (chưa giao)
        String status = order.getOrderStatus();
        if (!OrderStatus.PROCESSING.getValue().equals(status) && 
            !OrderStatus.VENDOR_CONFIRMED.getValue().equals(status) &&
            !OrderStatus.CONFIRMED.getValue().equals(status) &&
            !OrderStatus.SHIPPER_CANCELLED.getValue().equals(status)) {
            throw new IllegalStateException("Không thể hủy đơn ở trạng thái này");
        }
        
        // ✅ HOÀN XU nếu đã thanh toán bằng QR và vendor chưa xác nhận
        refundCoinsIfEligible(order);
        
        order.setOrderStatus(OrderStatus.CANCELLED.getValue());
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(cancelReason);
        order.setCancelledBy("CUSTOMER");
        
        orderRepository.save(order);
    }
    
    /**
     * Hoàn xu nếu đủ điều kiện:
     * - ĐÃ THANH TOÁN THÀNH CÔNG (Payment Status = Success)
     * - Bất kể phương thức thanh toán nào (COD, QR, COIN)
     * 
     * Logic: Nếu khách đã trả tiền → hoàn xu khi hủy/trả hàng
     */
    private void refundCoinsIfEligible(Order order) {
        try {
            // Lấy payment của order
            Payment payment = paymentRepository.findByOrderIdWithPaymentMethod(order.getId())
                    .orElse(null);
            
            if (payment == null) {
                logger.info("No payment found for order {}, skip coin refund", order.getId());
                return;
            }
            
            // ✅ HOÀN XU CHO TẤT CẢ ĐơN ĐÃ THANH TOÁN THÀNH CÔNG
            // - COD đã giao hàng và thanh toán → Success → hoàn xu
            // - QR đã thanh toán → Success → hoàn xu  
            // - COIN đã thanh toán → Success → hoàn xu
            // - COD chưa thanh toán → Pending → KHÔNG hoàn xu
            if ("Success".equals(payment.getPaymentStatus())) {
                User user = order.getUser();
                java.math.BigDecimal refundAmount = order.getTotalAmount();
                
                // Hoàn xu
                user.setCoins(user.getCoins().add(refundAmount));
                userRepository.save(user);
                
                String methodName = payment.getPaymentMethod().getMethodName();
                logger.info("✅ Refunded {} coins to user {} for order {} (Payment method: {})", 
                    refundAmount, user.getUserId(), order.getId(), methodName);
            } else {
                logger.info("Payment status is '{}', not 'Success'. Skip coin refund for order {}", 
                    payment.getPaymentStatus(), order.getId());
            }
        } catch (Exception e) {
            logger.error("❌ Error refunding coins for order {}: {}", order.getId(), e.getMessage());
            // Không throw exception - vẫn cho phép hủy đơn/trả hàng
        }
    }

    @Override
    @Transactional
    public void customerRequestReturn(Integer orderId, Integer userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra quyền sở hữu
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền yêu cầu trả hàng đơn này");
        }
        
        // Chỉ cho phép trả hàng khi đã giao
        if (!OrderStatus.DELIVERED.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Chỉ có thể trả hàng khi đơn đã được giao");
        }
        
        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(OrderStatus.RETURN_REQUESTED.getValue());
        
        // Tạo yêu cầu trả hàng
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrder(order);
        request.setUser(order.getUser());
        request.setReason(reason);
        request.setStatus("Pending");
        
        returnRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getCustomerOrders(Integer userId) {
        List<Order> orders = orderRepository.findByUserUserIdWithDetails(userId);
        return orders.stream()
                .map(this::mapToOrderDTOWithImages)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getShipperAssignedOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperIdAndOrderStatus(
                shipperId, OrderStatus.CONFIRMED.getValue());
        return orders.stream()
                .map(this::mapToOrderDTOWithImages)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getShipperDeliveringOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperIdAndOrderStatus(
                shipperId, OrderStatus.SHIPPING.getValue());
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = mapToOrderDTOWithImages(order);
                    // Lấy phương thức thanh toán
                    try {
                        paymentRepository.findByOrderIdWithPaymentMethod(order.getId()).ifPresent(payment -> {
                            if (payment.getPaymentMethod() != null) {
                                orderDTO.setPaymentMethod(payment.getPaymentMethod().getMethodCode());
                            }
                        });
                    } catch (Exception e) {
                        // Nếu không tìm thấy payment hoặc lỗi, đặt mặc định là COD
                        orderDTO.setPaymentMethod("COD");
                    }
                    return orderDTO;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getShipperAllOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperId(shipperId);
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = mapToOrderDTOWithImages(order);
                    
                    // Lấy phương thức thanh toán
                    try {
                        paymentRepository.findByOrderIdWithPaymentMethod(order.getId()).ifPresent(payment -> {
                            if (payment.getPaymentMethod() != null) {
                                orderDTO.setPaymentMethod(payment.getPaymentMethod().getMethodCode());
                            }
                        });
                    } catch (Exception e) {
                        // Nếu không tìm thấy payment hoặc lỗi, đặt mặc định là COD
                        orderDTO.setPaymentMethod("COD");
                    }
                    
                    // Lấy lý do trả hàng nếu có (bao gồm cả trường hợp đã reject)
                    if ("Return_Requested".equals(order.getOrderStatus()) || 
                        "Returned".equals(order.getOrderStatus()) ||
                        ("Delivered".equals(order.getOrderStatus()) && order.getAdminNotes() != null && order.getAdminNotes().contains("Lý do từ chối trả hàng"))) {
                        returnRequestRepository.findByOrderId(order.getId())
                            .stream()
                            .findFirst()
                            .ifPresent(returnRequest -> orderDTO.setReturnReason(returnRequest.getReason()));
                    }
                    return orderDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        OrderDTO orderDTO = mapToOrderDTOWithImages(order);
        
        // Explicitly map shipperId (ModelMapper có thể không tự map)
        if (order.getShipper() != null) {
            orderDTO.setShipperId(order.getShipper().getId());
        }
        
        // Lấy phương thức thanh toán
        try {
            paymentRepository.findByOrderIdWithPaymentMethod(orderId).ifPresent(payment -> {
                if (payment.getPaymentMethod() != null) {
                    orderDTO.setPaymentMethod(payment.getPaymentMethod().getMethodCode());
                }
            });
        } catch (Exception e) {
            // Nếu không tìm thấy payment hoặc lỗi, đặt mặc định là COD
            orderDTO.setPaymentMethod("COD");
        }
        
        // Lấy lý do trả hàng nếu có (bao gồm cả trường hợp đã reject)
        if ("Return_Requested".equals(order.getOrderStatus()) || 
            "Returned".equals(order.getOrderStatus()) ||
            ("Delivered".equals(order.getOrderStatus()) && order.getAdminNotes() != null && order.getAdminNotes().contains("Lý do từ chối trả hàng"))) {
            returnRequestRepository.findByOrderId(order.getId())
                .stream()
                .findFirst()
                .ifPresent(returnRequest -> orderDTO.setReturnReason(returnRequest.getReason()));
        }
        
        return orderDTO;
    }
    
    // Helper method to map Order to OrderDTO with product images
    private OrderDTO mapToOrderDTOWithImages(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        
        // Map productImage for each OrderItemDTO
        if (orderDTO.getOrderItems() != null && order.getOrderItems() != null) {
            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getId().equals(itemDTO.getId())) {
                        if (item.getProduct() != null && item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                            // Assuming ProductImage has a getImageUrl() method; adjust if the actual getter is different (e.g., getUrl())
                            itemDTO.setProductImage(item.getProduct().getImages().iterator().next().getImageUrl());
                        }
                        break;
                    }
                }
            }
        }
        
        return orderDTO;
    }
}