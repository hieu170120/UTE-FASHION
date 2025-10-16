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
            System.out.println("Calling SePay API: " + url);
            
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
            
            System.out.println("API Response Status: " + response.getStatusCode());
            System.out.println("API Response Body: " + response.getBody());
            
            SePayTransactionDTO result = objectMapper.readValue(response.getBody(), SePayTransactionDTO.class);
            System.out.println("Parsed " + (result.getTransactions() != null ? result.getTransactions().size() : 0) + " transactions");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("\u274c Error calling SePay API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SePayTransactionDTO.Transaction findMatchingTransaction(
            String orderNumber, 
            BigDecimal amount, 
            long startTime) {
        
        try {
            System.out.println("\n=== CHECKING SEPAY TRANSACTION ===");
            System.out.println("Order Number: " + orderNumber);
            System.out.println("Amount: " + amount);
            System.out.println("Start Time: " + new java.util.Date(startTime));
            
            SePayTransactionDTO response = getRecentTransactions(20);
            
            if (response == null || response.getTransactions() == null) {
                System.out.println("No transactions returned from API");
                return null;
            }
            
            System.out.println("Found " + response.getTransactions().size() + " transactions");
            
            // Format: ORD-2024-1234 → ORD20241234 (để search)
            String searchContent = orderNumber.replace("-", "").toUpperCase();
            System.out.println("Searching for content: " + searchContent);
            
            for (SePayTransactionDTO.Transaction tx : response.getTransactions()) {
                System.out.println("\nChecking TX " + tx.getId() + ":");
                System.out.println("  Amount: " + tx.getAmountIn() + " (need: " + amount + ")");
                System.out.println("  Content: " + tx.getTransactionContent());
                System.out.println("  Date: " + tx.getTransactionDate());
                
                // Check amount
                if (tx.getAmountIn() == null || 
                    tx.getAmountIn().compareTo(amount) != 0) {
                    System.out.println("  \u274c Amount mismatch");
                    continue;
                }
                
                // Check content
                String content = tx.getTransactionContent();
                if (content == null || 
                    !content.toUpperCase().contains(searchContent)) {
                    System.out.println("  \u274c Content mismatch");
                    continue;
                }
                
                // Check transaction time (phải sau startTime)
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime txTime = LocalDateTime.parse(tx.getTransactionDate(), formatter);
                    long txTimestamp = java.sql.Timestamp.valueOf(txTime).getTime();
                    
                    if (txTimestamp >= startTime) {
                        System.out.println("  \u2705 MATCH FOUND!");
                        return tx; // ✅ FOUND!
                    } else {
                        System.out.println("  \u274c Transaction too old");
                    }
                } catch (Exception e) {
                    System.out.println("  \u274c Date parse error: " + e.getMessage());
                    // Try without time check
                    System.out.println("  \u26a0\ufe0f Accepting without time validation");
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
