package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Shipper_Cancel_History")
@Getter
@Setter
public class ShipperCancelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason; // Lý do hủy

    @CreationTimestamp
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
