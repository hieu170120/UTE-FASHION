package com.example.demo.service.impl;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.entity.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private ShipperRepository shipperRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Integer userId, String sessionId, OrderDTO orderDTO) {
        Cart cart = userId != null
                ? cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId))
                : cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session: " + sessionId));

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUser(cart.getUser());
        order.setRecipientName(orderDTO.getRecipientName());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setEmail(orderDTO.getEmail());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setWard(orderDTO.getWard());
        order.setDistrict(orderDTO.getDistrict());
        order.setCity(orderDTO.getCity());
        order.setPostalCode(orderDTO.getPostalCode());
        order.setOrderDate(LocalDateTime.now());
        order.setCustomerNotes(orderDTO.getCustomerNotes());

        // Set carrier and shipping fee
        Integer carrierId = orderDTO.getCarrierId();
        if (carrierId != null) {
            Carrier carrier = carrierRepository.findById(carrierId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carrier not found: " + carrierId));
            order.setCarrier(carrier);
            order.setShippingFee(carrier.getDefaultShippingFee());
        } else {
            // Set default shipping fee if no carrier selected
            order.setShippingFee(BigDecimal.valueOf(30000)); // Default 30k
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setProductName(cartItem.getProduct().getProductName());
            orderItem.setProductSku(cartItem.getProduct().getSku());
            if (cartItem.getVariant() != null) {
                orderItem.setSize(cartItem.getVariant().getSize().getSizeName());
                orderItem.setColor(cartItem.getVariant().getColor().getColorName());
            }
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItem.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getOrderItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());

            // Update inventory
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getShippingFee()).subtract(order.getDiscountAmount()).add(order.getTaxAmount()));
        // Initial status: Processing (Đang xử lý)
        order.setOrderStatus(OrderStatus.PROCESSING.getValue());
        orderRepository.save(order);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        // Save order status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setNewStatus(OrderStatus.PROCESSING.getValue());
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return mapToOrderDTO(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(Integer userId) {
        return orderRepository.findByUser_UserId(userId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderDTO);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, String newStatus, String notes, Integer changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(order.getOrderStatus());
        history.setNewStatus(newStatus);
        history.setNotes(notes);
        history.setChangedAt(LocalDateTime.now());
        if (changedBy != null) {
            User user = new User();
            user.setUserId(changedBy);
            history.setChangedBy(user);
        }
        statusHistoryRepository.save(history);

        order.setOrderStatus(newStatus);
        if (newStatus.equals(OrderStatus.CONFIRMED.getValue())) order.setConfirmedAt(LocalDateTime.now());
        else if (newStatus.equals(OrderStatus.SHIPPING.getValue())) order.setShippedAt(LocalDateTime.now());
        else if (newStatus.equals(OrderStatus.DELIVERED.getValue())) order.setDeliveredAt(LocalDateTime.now());
        else if (newStatus.equals(OrderStatus.CANCELLED.getValue())) order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }
        String currentStatus = order.getOrderStatus();
        if (!currentStatus.equals(OrderStatus.PROCESSING.getValue()) && !currentStatus.equals(OrderStatus.CONFIRMED.getValue())) {
            throw new RuntimeException("Cannot cancel order in status: " + currentStatus);
        }
        updateOrderStatus(orderId, OrderStatus.CANCELLED.getValue(), "Cancelled by user", userId);
        // If already assigned shipper, clear it
        order.setShipper(null);
        orderRepository.save(order);
    }

    @Transactional
    public void assignShipperAndConfirm(Integer orderId, Integer shipperId, Integer adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getOrderStatus().equals(OrderStatus.PROCESSING.getValue())) {
            throw new RuntimeException("Order not in processing status");
        }
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found: " + shipperId));
        if (!shipper.getCarrier().getId().equals(order.getCarrier().getId())) {
            throw new RuntimeException("Shipper does not match carrier");
        }
        order.setShipper(shipper);
        updateOrderStatus(orderId, OrderStatus.CONFIRMED.getValue(), "Assigned shipper and confirmed by admin", adminId);
    }

    public List<OrderDTO> getOrdersByShipper(Integer shipperId) {
        return orderRepository.findByUser_UserId(shipperId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getShipperOrderStats(Integer shipperId) {
        // Example stats: count by status
        List<Order> orders = orderRepository.findByUser_UserId(shipperId);
        long total = orders.size();
        long delivered = orders.stream().filter(o -> o.getOrderStatus().equals(OrderStatus.DELIVERED.getValue())).count();
        long cancelled = orders.stream().filter(o -> o.getOrderStatus().equals(OrderStatus.CANCELLED.getValue())).count();
        return Map.of("total", total, "delivered", delivered, "cancelled", cancelled);
    }

    @Transactional
    public void shipperConfirmOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (!order.getOrderStatus().equals(OrderStatus.CONFIRMED.getValue())) {
            throw new RuntimeException("Order not in confirmed status");
        }
        updateOrderStatus(orderId, OrderStatus.SHIPPING.getValue(), "Shipper confirmed and started shipping", order.getShipper().getUser().getUserId());
        // Set random shipping time 2-5 minutes for simulation
        Random random = new Random();
        int randomMin = random.nextInt(4) + 2; // 2 to 5
        order.setShippingTime(randomMin * 60); // in seconds
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(randomMin));
        orderRepository.save(order);
    }

    @Transactional
    public void shipperCancelOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (!order.getOrderStatus().equals(OrderStatus.CONFIRMED.getValue())) {
            throw new RuntimeException("Order not in confirmed status");
        }
        Shipper shipper = order.getShipper();
        if (shipper.getCancelCount() >= 3) {
            throw new RuntimeException("Reached cancel limit");
        }
        shipper.setCancelCount(shipper.getCancelCount() + 1);
        shipperRepository.save(shipper);
        order.setShipper(null);
        updateOrderStatus(orderId, OrderStatus.PROCESSING.getValue(), "Shipper cancelled assignment", shipper.getUser().getUserId());
    }

    @Transactional
    public void requestReturn(Integer orderId, Integer userId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (!order.getOrderStatus().equals(OrderStatus.DELIVERED.getValue())) {
            throw new RuntimeException("Order not delivered");
        }
        updateOrderStatus(orderId, "Return Requested", notes, userId);
    }

    @Transactional
    public void approveReturn(Integer orderId, String notes, Integer adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getOrderStatus().equals("Return Requested")) {
            throw new RuntimeException("No return requested");
        }
        updateOrderStatus(orderId, OrderStatus.REFUNDED.getValue(), notes, adminId);
        // Additional refund logic if needed
    }

    @Transactional
    public void rejectReturn(Integer orderId, String notes, Integer adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getOrderStatus().equals("Return Requested")) {
            throw new RuntimeException("No return requested");
        }
        updateOrderStatus(orderId, OrderStatus.DELIVERED.getValue(), notes, adminId);
    }

    // Simulate delivery after timer (could be called by scheduler)
    @Transactional
    public void completeDelivery(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (order.getOrderStatus().equals(OrderStatus.SHIPPING.getValue()) && LocalDateTime.now().isAfter(order.getEstimatedDeliveryTime())) {
            updateOrderStatus(orderId, OrderStatus.DELIVERED.getValue(), "Delivery completed", null);
        }
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setOrderItems(order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));
        if (order.getCarrier() != null) {
            orderDTO.setUserId(order.getCarrier().getId());
        }
        if (order.getShipper() != null) {
            orderDTO.setUserId(order.getShipper().getId());
        }
        return orderDTO;
    }
}