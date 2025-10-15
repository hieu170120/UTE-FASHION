package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Coupons")
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Integer id;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // Percentage, Fixed

    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 18, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private User vendor; // NULL nếu là mã của Admin

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser = 1;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_shipping_discount")
    private boolean isShippingDiscount = false; // Mã giảm phí vận chuyển

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
