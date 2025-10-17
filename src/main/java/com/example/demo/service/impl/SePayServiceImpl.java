package com.example.demo.service.impl;

import com.example.demo.dto.SePayTransactionDTO;
import com.example.demo.service.SePayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SePayServiceImpl
 * Implementation cho SePay API integration
 * Xử lý verify thanh toán QR banking
 */
@Service
public class SePayServiceImpl implements SePayService {

    @Value("${sepay.api.url}")
    private String apiUrl;

    @Value("${sepay.api.key}")
    private String apiKey;

    @Value("${sepay.bank.account}")
    private String bankAccount;

    @Value("${sepay.bank.name}")
    private String bankName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SePayTransactionDTO getRecentTransactions(int limit) {
        try {
            String url = apiUrl + "/transactions/list?limit=" + limit;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), SePayTransactionDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error calling SePay API: " + e.getMessage());
            return null;
        }
    }

    @Override
    public SePayTransactionDTO.Transaction findMatchingTransaction(
            String orderNumber, 
            BigDecimal amount, 
            long startTime) {
        
        try {
            SePayTransactionDTO response = getRecentTransactions(20);
            
            if (response == null || response.getTransactions() == null) {
                return null;
            }
            
            String searchContent = orderNumber.replace("-", "").toUpperCase();
            
            for (SePayTransactionDTO.Transaction tx : response.getTransactions()) {
                if (tx.getAmountIn() == null || tx.getAmountIn().compareTo(amount) != 0) {
                    continue;
                }
                
                String content = tx.getTransactionContent();
                if (content == null || !content.toUpperCase().contains(searchContent)) {
                    continue;
                }
                
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime txTime = LocalDateTime.parse(tx.getTransactionDate(), formatter);
                    long txTimestamp = java.sql.Timestamp.valueOf(txTime).getTime();
                    
                    if (txTimestamp >= startTime) {
                        return tx;
                    }
                } catch (Exception e) {
                    return tx;
                }
            }
            
            return null; // Not found
            
        } catch (Exception e) {
            System.err.println("Error finding matching transaction: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String generateQRCodeUrl(String orderNumber, BigDecimal amount) {
        try {
            // Nội dung: UTEFASHION ORD20241234
            String content = "UTEFASHION " + orderNumber.replace("-", "");
            String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8.toString());
            
            // VietQR.io URL
            String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s",
                bankName,
                bankAccount,
                amount.intValue(),
                encodedContent
            );
            
            return qrUrl;
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR URL", e);
        }
    }
}
