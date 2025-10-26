package com.example.demo.repository;

import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Payment Repository
 * Data access layer cho Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    /**
     * Tìm payment theo order ID
     */
    Optional<Payment> findByOrder_Id(Integer orderId);
    
    /**
     * Tìm payment theo order ID với JOIN FETCH PaymentMethod
     */
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p JOIN FETCH p.paymentMethod WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithPaymentMethod(@org.springframework.data.repository.query.Param("orderId") Integer orderId);
    
    /**
     * Tìm payment theo transaction ID (SePay transaction)
     */
    Optional<Payment> findByTransactionId(String transactionId);
}
