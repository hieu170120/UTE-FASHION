package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ConversionAnalytics")
@Getter
@Setter
public class ConversionAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversion_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "period_type", nullable = false, length = 10)
    private String periodType; // 'DAY', 'WEEK', 'MONTH'

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "view_count")
    private Integer viewCount = 0; // lượt xem sản phẩm

    @Column(name = "add_to_cart_count")
    private Integer addToCartCount = 0; // lượt thêm giỏ

    @Column(name = "checkout_count")
    private Integer checkoutCount = 0; // lượt thanh toán

    @Column(name = "completed_count")
    private Integer completedCount = 0; // đơn hoàn tất

    @Column(name = "view_change", precision = 5, scale = 2)
    private BigDecimal viewChange; // % tăng trưởng so với kỳ trước

    @Column(name = "cart_change", precision = 5, scale = 2)
    private BigDecimal cartChange;

    @Column(name = "checkout_change", precision = 5, scale = 2)
    private BigDecimal checkoutChange;

    @Column(name = "completed_change", precision = 5, scale = 2)
    private BigDecimal completedChange;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
