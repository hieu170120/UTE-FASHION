package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Carriers")
@Getter
@Setter
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrier_id")
    private Integer id;

    @Column(name = "carrier_name", nullable = false, unique = true, length = 100)
    private String carrierName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "default_shipping_fee", precision = 18, scale = 2)
    private BigDecimal defaultShippingFee = BigDecimal.ZERO;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
