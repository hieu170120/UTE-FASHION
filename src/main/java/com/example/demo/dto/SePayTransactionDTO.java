package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SePay API Response DTO
 * API: https://my.sepay.vn/userapi/transactions/list
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SePayTransactionDTO {
    
    private int status;
    private Object error;
    private Object messages;
    private List<Transaction> transactions;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        private Long id;
        
        @JsonProperty("transaction_date")
        private String transactionDate;
        
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("amount_in")
        private BigDecimal amountIn;
        
        @JsonProperty("amount_out")
        private BigDecimal amountOut;
        
        @JsonProperty("transaction_content")
        private String transactionContent;
        
        @JsonProperty("bank_brand_name")
        private String bankBrandName;
        
        @JsonProperty("reference_number")
        private String referenceNumber;
    }
}
