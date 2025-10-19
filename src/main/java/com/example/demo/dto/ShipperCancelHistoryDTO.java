package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ShipperCancelHistoryDTO {
    private Integer id;
    private Integer orderId;
    private String orderNumber;
    private String shipperName;
    private String reason;
    private LocalDateTime cancelledAt;
    private String orderStatus; // Trạng thái hiện tại của đơn hàng
    private String recipientName;
    private BigDecimal totalAmount;
}
