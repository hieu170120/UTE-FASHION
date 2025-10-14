package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PaymentMethod Entity - Phương thức thanh toán
 * COD, SEPAY_QR
 */
@Entity
@Table(name = "Payment_Methods")
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Integer id;

    @Column(name = "method_name", nullable = false, unique = true, length = 100)
    private String methodName;

    @Column(name = "method_code", nullable = false, unique = true, length = 50)
    private String methodCode; // COD, SEPAY_QR

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
