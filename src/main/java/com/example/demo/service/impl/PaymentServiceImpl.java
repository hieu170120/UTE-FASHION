package com.example.demo.service.impl;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentMethodRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentServiceImpl
 * Implementation cho Payment business logic
 * Xử lý COD và SePay QR payments
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findByIsActiveTrue();
    }

    @Override
    public PaymentMethod getPaymentMethodByCode(String code) {
        return paymentMethodRepository.findByMethodCode(code)
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + code));
    }

    @Override
    @Transactional
    public PaymentDTO createCODPayment(Integer orderId) {
        // Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Lấy COD payment method
        PaymentMethod codMethod = getPaymentMethodByCode("COD");

        // Tạo payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(codMethod);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus("Pending"); // COD starts as Pending
        payment.setTransactionId("COD-" + order.getOrderNumber());
        // paidAt sẽ được set khi giao hàng thành công

        Payment savedPayment = paymentRepository.save(payment);

        // Update order payment status
        order.setPaymentStatus("Unpaid");
        orderRepository.save(order);

        return convertToDTO(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDTO createSePayPayment(Integer orderId, String transactionId, String gatewayResponse) {
        // Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Lấy SePay payment method
        PaymentMethod sePayMethod = getPaymentMethodByCode("SEPAY_QR");

        // Tạo payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(sePayMethod);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus("Success"); // SePay is already verified
        payment.setTransactionId(transactionId);
        payment.setPaymentGatewayResponse(gatewayResponse);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update order payment status
        order.setPaymentStatus("Paid");
        orderRepository.save(order);

        return convertToDTO(savedPayment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Integer orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElse(null);
        
        if (payment == null) {
            return null;
        }
        
        return convertToDTO(payment);
    }

    /**
     * Convert Payment entity to PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setOrderNumber(payment.getOrder().getOrderNumber());
        dto.setPaymentMethodId(payment.getPaymentMethod().getId());
        dto.setPaymentMethodName(payment.getPaymentMethod().getMethodName());
        dto.setTransactionId(payment.getTransactionId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
