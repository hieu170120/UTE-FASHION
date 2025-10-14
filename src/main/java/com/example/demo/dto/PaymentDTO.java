package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Integer id;
    private Integer orderId;
    private String orderNumber;
    private Integer paymentMethodId;
    private String paymentMethodName;
    private String transactionId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
