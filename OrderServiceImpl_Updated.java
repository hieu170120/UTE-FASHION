// UPDATED: OrderServiceImpl.java - Cập nhật để phù hợp với quy trình mới

@Service
public class OrderServiceImpl implements OrderService {

    // ... existing code ...

    /**
     * ADMIN: Chọn shipper và xác nhận đơn hàng
     * Processing → Confirmed
     */
    @Override
    @Transactional
    public OrderDTO adminConfirmOrderWithShipper(Integer orderId, Integer shipperId, Integer adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        // Chỉ cho phép khi đơn hàng ở trạng thái "Processing"
        if (!OrderStatus.PROCESSING.getValue().equals(order.getOrderStatus())) {
            throw new RuntimeException("Order must be in Processing status to assign shipper");
        }
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found: " + shipperId));
        
        // Kiểm tra shipper có thuộc carrier phù hợp không
        if (!shipper.getCarrier().getId().equals(order.getCarrier().getId())) {
            throw new RuntimeException("Shipper does not belong to the selected carrier");
        }
        
        // Assign shipper và chuyển trạng thái
        order.setShipper(shipper);
        order.setConfirmedAt(LocalDateTime.now());
        
        return updateOrderStatus(orderId, OrderStatus.CONFIRMED.getValue(), 
            "Admin confirmed order and assigned shipper", adminId);
    }
    
    /**
     * SHIPPER: Xác nhận nhận đơn và bắt đầu giao hàng
     * Confirmed → Shipping (với countdown timer)
     */
    @Override
    @Transactional
    public OrderDTO shipperConfirmDelivery(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        // Validate shipper ownership và order status
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Unauthorized: Not your assigned order");
        }
        
        if (!OrderStatus.CONFIRMED.getValue().equals(order.getOrderStatus())) {
            throw new RuntimeException("Order must be Confirmed to start shipping");
        }
        
        // Tạo random thời gian giao hàng 2-5 phút
        Random random = new Random();
        int deliveryMinutes = random.nextInt(4) + 2; // 2-5 minutes
        
        order.setShippedAt(LocalDateTime.now());
        order.setShippingTime(deliveryMinutes * 60); // seconds
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(deliveryMinutes));
        
        return updateOrderStatus(orderId, OrderStatus.SHIPPING.getValue(), 
            "Shipper confirmed and started delivery", 
            order.getShipper().getUser().getUserId());
    }
    
    /**
     * SHIPPER: Hủy đơn hàng (với giới hạn 3 lần)
     * Confirmed → Processing (quay lại cho admin chọn shipper khác)
     */
    @Override
    @Transactional
    public void shipperCancelOrder(Integer orderId, Integer shipperId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Unauthorized: Not your assigned order");
        }
        
        if (!OrderStatus.CONFIRMED.getValue().equals(order.getOrderStatus())) {
            throw new RuntimeException("Can only cancel confirmed orders");
        }
        
        Shipper shipper = order.getShipper();
        
        // Kiểm tra giới hạn hủy đơn (3 lần)
        if (shipper.getCancelCount() >= 3) {
            throw new RuntimeException("Cannot cancel more orders. You have reached the maximum limit (3 cancellations).");
        }
        
        // Tăng cancel count
        shipper.setCancelCount(shipper.getCancelCount() + 1);
        shipperRepository.save(shipper);
        
        // Remove shipper assignment và quay về trạng thái Processing
        order.setShipper(null);
        order.setConfirmedAt(null);
        
        updateOrderStatus(orderId, OrderStatus.PROCESSING.getValue(), 
            "Shipper cancelled assignment: " + reason, 
            shipper.getUser().getUserId());
    }
    
    /**
     * CUSTOMER: Hủy đơn hàng (chỉ khi Processing hoặc Confirmed)
     */
    @Override
    @Transactional
    public void customerCancelOrder(Integer orderId, Integer userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }
        
        String currentStatus = order.getOrderStatus();
        if (!OrderStatus.PROCESSING.getValue().equals(currentStatus) && 
            !OrderStatus.CONFIRMED.getValue().equals(currentStatus)) {
            throw new RuntimeException("Cannot cancel order in status: " + currentStatus);
        }
        
        // Clear shipper assignment if exists
        if (order.getShipper() != null) {
            order.setShipper(null);
        }
        
        order.setCancelledAt(LocalDateTime.now());
        updateOrderStatus(orderId, OrderStatus.CANCELLED.getValue(), 
            "Customer cancelled order: " + reason, userId);
    }
    
    /**
     * CUSTOMER: Yêu cầu trả hàng (chỉ khi Delivered)
     */
    @Override
    @Transactional
    public void requestReturn(Integer orderId, Integer userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to request return for this order");
        }
        
        if (!OrderStatus.DELIVERED.getValue().equals(order.getOrderStatus())) {
            throw new RuntimeException("Can only request return for delivered orders");
        }
        
        updateOrderStatus(orderId, "RETURN_REQUESTED", 
            "Customer requested return: " + reason, userId);
    }
    
    /**
     * ADMIN: Xử lý yêu cầu trả hàng
     */
    @Override
    @Transactional
    public void processReturnRequest(Integer orderId, Integer adminId, boolean approved, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        if (!"RETURN_REQUESTED".equals(order.getOrderStatus())) {
            throw new RuntimeException("No return request found for this order");
        }
        
        if (approved) {
            updateOrderStatus(orderId, OrderStatus.REFUNDED.getValue(), 
                "Return request approved: " + notes, adminId);
            // TODO: Process actual refund logic here
        } else {
            updateOrderStatus(orderId, OrderStatus.DELIVERED.getValue(), 
                "Return request rejected: " + notes, adminId);
        }
    }
    
    /**
     * SYSTEM: Auto complete delivery khi hết thời gian
     */
    @Override
    @Transactional
    public void autoCompleteDelivery(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        
        if (OrderStatus.SHIPPING.getValue().equals(order.getOrderStatus()) && 
            order.getEstimatedDeliveryTime() != null && 
            LocalDateTime.now().isAfter(order.getEstimatedDeliveryTime())) {
            
            order.setDeliveredAt(LocalDateTime.now());
            updateOrderStatus(orderId, OrderStatus.DELIVERED.getValue(), 
                "Delivery completed automatically", null);
        }
    }
    
    /**
     * Get orders by status for admin
     */
    @Override
    public List<OrderDTO> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get shippers by carrier for admin order assignment
     */
    @Override
    public List<ShipperDTO> getAvailableShippersByCarrier(Integer carrierId) {
        return shipperRepository.findByCarrierIdAndIsActiveTrue(carrierId).stream()
                .filter(shipper -> shipper.getCancelCount() < 3) // Exclude banned shippers
                .map(shipper -> modelMapper.map(shipper, ShipperDTO.class))
                .collect(Collectors.toList());
    }
}