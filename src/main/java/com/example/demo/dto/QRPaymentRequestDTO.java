package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QRPaymentRequestDTO {
    private String orderNumber;
    private BigDecimal amount;
    private String content; // UTEFASHION ORD20241234
}
