package com.example.demo.service.impl;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderReturnRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
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

@Service
public class OrderManagementServiceImpl implements OrderManagementService {

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
    
    private static final int MAX_SHIPPER_CANCEL_COUNT = 3;
    private static final Random random = new Random();

    // === ADMIN FUNCTIONS ===

    @Override
    public List<OrderDTO> getPendingOrders() {
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.PROCESSING.getValue());
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void adminConfirmOrderAndAssignShipper(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shipper"));
        
        // Kiểm tra trạng thái đơn hàng
        if (!OrderStatus.PROCESSING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xử lý");
        }
        
        // Gán shipper và cập nhật trạng thái
        order.setShipper(shipper);
        order.setOrderStatus(OrderStatus.CONFIRMED.getValue());
        order.setConfirmedAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        // Gửi thông báo cho shipper
        notificationService.notifyShipperNewOrder(shipper, order);
    }

    @Override
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
        order.setOrderStatus(OrderStatus.RETURNED.getValue());
        
        returnRequestRepository.save(request);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void rejectReturnRequest(Integer requestId) {
        OrderReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu"));
        
        request.setStatus("Rejected");
        request.setRejectedAt(LocalDateTime.now());
        
        returnRequestRepository.save(request);
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
        
        // Quay lại trạng thái chờ admin chọn shipper khác
        order.setOrderStatus(OrderStatus.PROCESSING.getValue());
        order.setShipper(null);
        order.setConfirmedAt(null);
        
        orderRepository.save(order);
    }

    @Override
    public Long getShipperCancelCount(Integer shipperId) {
        return cancelHistoryRepository.countByShipperId(shipperId);
    }


    @Override
    @Transactional
    public void markOrderAsDelivered(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        order.setOrderStatus(OrderStatus.DELIVERED.getValue());
        order.setDeliveredAt(LocalDateTime.now());
        
        orderRepository.save(order);
    }

    // === CUSTOMER FUNCTIONS ===

    @Override
    @Transactional
    public void customerCancelOrder(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra quyền sở hữu
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn này");
        }
        
        // Chỉ cho phép hủy khi đơn đang xử lý hoặc đã xử lý (chưa giao)
        String status = order.getOrderStatus();
        if (!OrderStatus.PROCESSING.getValue().equals(status) && 
            !OrderStatus.CONFIRMED.getValue().equals(status)) {
            throw new IllegalStateException("Không thể hủy đơn ở trạng thái này");
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED.getValue());
        order.setCancelledAt(LocalDateTime.now());
        
        orderRepository.save(order);
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
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getShipperAssignedOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperIdAndOrderStatus(
                shipperId, OrderStatus.CONFIRMED.getValue());
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getShipperDeliveringOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperIdAndOrderStatus(
                shipperId, OrderStatus.SHIPPING.getValue());
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderDTO> getShipperAllOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findByShipperId(shipperId);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return modelMapper.map(order, OrderDTO.class);
    }
}
