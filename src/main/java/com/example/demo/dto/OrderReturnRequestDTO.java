package com.example.demo.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class OrderReturnRequestDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    
    private Integer orderId;
    
    private String orderCode;
    
    private Integer userId;
    
    private String userName;
    
    private String userEmail;
    
    private String userPhone;
    
    private String reason;
    
    private String notes;
    
    private String status; // PENDING, APPROVED, REJECTED
    
    private LocalDateTime requestDate;
    
    private LocalDateTime approvedDate;
    
    private Integer approvedBy;
    
    private String approverName;
    
    private String refundStatus; // PENDING, PROCESSING, COMPLETED
    
    private Double refundAmount;
    
    private String refundMethod; // BANK_TRANSFER, WALLET, ORIGINAL_METHOD
    
    private String attachmentUrl;
    
    private String rejectReason;
}