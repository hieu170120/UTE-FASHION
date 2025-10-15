package com.example.demo.service.impl;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.entity.*;
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
        orderRepository.save(order);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        // Save order status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setNewStatus("Pending");
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
        if (newStatus.equals("Confirmed")) order.setConfirmedAt(LocalDateTime.now());
        else if (newStatus.equals("Shipping")) order.setShippedAt(LocalDateTime.now());
        else if (newStatus.equals("Delivered")) order.setDeliveredAt(LocalDateTime.now());
        else if (newStatus.equals("Cancelled")) order.setCancelledAt(LocalDateTime.now());
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
        updateOrderStatus(orderId, "Cancelled", "Cancelled by user", userId);
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setOrderItems(order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));
        return orderDTO;
    }
}
