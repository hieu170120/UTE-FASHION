package com.example.demo.service;

import com.example.demo.dto.SePayTransactionDTO;
import java.math.BigDecimal;

/**
 * SePayService Interface
 * Tích hợp với SePay API để verify thanh toán QR
 */
public interface SePayService {
    
    /**
     * Gọi SePay API để lấy danh sách transactions gần đây
     * @param limit - Số lượng transaction cần lấy
     * @return SePayTransactionDTO chứa list transactions
     */
    SePayTransactionDTO getRecentTransactions(int limit);
    
    /**
     * Check xem có transaction nào match với order không
     * Match conditions:
     * - amount_in == order amount
     * - content contains order number (ORD20241234)
     * - transaction_date >= startTime (sau khi QR được tạo)
     * 
     * @param orderNumber - Order number (ORD-2024-1234)
     * @param amount - Số tiền cần check
     * @param startTime - Thời điểm bắt đầu polling (timestamp)
     * @return Transaction nếu tìm thấy, null nếu không tìm thấy
     */
    SePayTransactionDTO.Transaction findMatchingTransaction(
        String orderNumber, 
        BigDecimal amount, 
        long startTime
    );
    
    /**
     * Generate QR Code URL từ VietQR.io
     * Format: https://img.vietqr.io/image/BIDV-96247NMB0A-compact2.png?amount=500000&addInfo=UTEFASHION%20ORD20241234
     * 
     * @param orderNumber - Order number (ORD-2024-1234)
     * @param amount - Số tiền thanh toán
     * @return QR Code URL
     */
    String generateQRCodeUrl(String orderNumber, BigDecimal amount);
}
