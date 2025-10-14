package com.example.demo.repository;

import com.example.demo.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PaymentMethod Repository
 * Data access layer cho PaymentMethod entity
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    
    /**
     * Tìm payment method theo code (COD, SEPAY_QR)
     */
    Optional<PaymentMethod> findByMethodCode(String methodCode);
    
    /**
     * Lấy tất cả payment methods đang active
     */
    List<PaymentMethod> findByIsActiveTrue();
}
