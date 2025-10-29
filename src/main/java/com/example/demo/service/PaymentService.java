package com.example.demo.service;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.entity.PaymentMethod;
import java.util.List;

/**
 * PaymentService Interface
 * Xử lý logic nghiệp vụ cho Payment
 */
public interface PaymentService {
    
    /**
     * Lấy tất cả payment methods đang active
     * @return List of active payment methods
     */
    List<PaymentMethod> getAllPaymentMethods();
    
    /**
     * Lấy payment method theo code (COD, SEPAY_QR)
     * @param code - Payment method code
     * @return PaymentMethod
     */
    PaymentMethod getPaymentMethodByCode(String code);
    
    /**
     * Tạo payment cho COD (Cash on Delivery)
     * Status: Pending
     * @param orderId - Order ID
     * @return PaymentDTO
     */
    PaymentDTO createCODPayment(Integer orderId);
    
    /**
     * Tạo payment cho SePay QR
     * Status: Success (vì đã verify transaction)
     * @param orderId - Order ID
     * @param transactionId - SePay transaction ID
     * @param gatewayResponse - Response từ SePay API
     * @return PaymentDTO
     */
    PaymentDTO createSePayPayment(Integer orderId, String transactionId, String gatewayResponse);
    
    /**
     * Tạo payment cho COIN (thanh toán bằng xu)
     * Status: Success
     * Trừ xu từ tài khoản user
     * @param orderId - Order ID
     * @param userId - User ID
     * @return PaymentDTO
     */
    PaymentDTO createCoinPayment(Integer orderId, Integer userId);
    
    /**
     * Lấy payment theo order ID
     * @param orderId - Order ID
     * @return PaymentDTO
     */
    PaymentDTO getPaymentByOrderId(Integer orderId);
}
