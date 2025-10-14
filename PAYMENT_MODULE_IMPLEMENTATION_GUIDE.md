# 💳 PAYMENT MODULE IMPLEMENTATION GUIDE
## COD + SePay QR Banking với Polling

---

## 📊 **FLOW TỔNG QUAN**

### **Flow COD:**
```
Checkout → Chọn COD → TẠO ORDER → TẠO PAYMENT (Pending) → Success
```

### **Flow SePay QR:**
```
1. Checkout → Chọn SePay QR
   ↓
2. Generate Order Number (ORD-2024-1234) ⭐ CHƯA LƯU DB
   Lưu vào Session
   ↓
3. Generate QR Code:
   - Bank: BIDV (96247NMB0A)
   - Amount: 500000
   - Content: UTEFASHION ORD20241234
   ↓
4. Hiển thị QR + Start Polling (60s)
   ↓
5. Polling mỗi 3s gọi SePay API:
   GET /userapi/transactions/list
   Check: amount_in == 500000 && content contains "ORD20241234"
   ↓
6a. FOUND (trong 60s):
    ✅ TẠO ORDER vào DB
    ✅ TẠO PAYMENT (Success)
    ✅ XÓA CART
    → Success page
    
6b. TIMEOUT (60s):
    ❌ KHÔNG TẠO ORDER
    ❌ GIỮ CART
    → Error page
```

---

## 🗂️ **CẤU TRÚC FILE CẦN TẠO**

```
src/main/java/com/example/demo/
├── entity/
│   ├── Payment.java ✅ (Đã tạo)
│   └── PaymentMethod.java ✅ (Đã tạo)
├── dto/
│   ├── SePayTransactionDTO.java ✅ (Đã tạo)
│   ├── PaymentDTO.java ⭐ CẦN TẠO
│   └── QRPaymentRequestDTO.java ⭐ CẦN TẠO
├── repository/
│   ├── PaymentRepository.java ⭐ CẦN TẠO
│   └── PaymentMethodRepository.java ⭐ CẦN TẠO
├── service/
│   ├── PaymentService.java ⭐ CẦN TẠO
│   ├── SePayService.java ⭐ CẦN TẠO
│   └── impl/
│       ├── PaymentServiceImpl.java ⭐ CẦN TẠO
│       └── SePayServiceImpl.java ⭐ CẦN TẠO
├── controller/
│   └── PaymentController.java ⭐ CẦN TẠO
└── config/
    └── SePayConfig.java ⭐ CẦN TẠO

src/main/resources/
├── application.properties ⭐ CẦN SỬA
└── templates/
    └── payment/
        ├── method-selection.html ⭐ CẦN TẠO
        ├── qr-payment.html ⭐ CẦN TẠO
        └── payment-failed.html ⭐ CẦN TẠO

database/
└── Insert_Payment_Methods.sql ⭐ CẦN CHẠY
```

---

## 🔧 **1. CẤU HÌNH**

### **application.properties:**
```properties
# SePay Configuration
sepay.api.url=https://my.sepay.vn/userapi
sepay.api.key=POP0SR3X7EEE0DYHSJKM1MHJBGFEMTUL4IIQLSNNIPZU59WPRNAAFVQWQZ8QWBJC
sepay.bank.account=96247NMB0A
sepay.bank.name=BIDV
sepay.polling.interval=3000
sepay.polling.timeout=60000
```

---

## 💾 **2. DATABASE**

### **Insert Payment Methods:**
```sql
USE UTE_Fashion;
GO

DELETE FROM Payment_Methods;
GO

INSERT INTO Payment_Methods (method_name, method_code, description, is_active, display_order) 
VALUES 
(N'Thanh toán khi nhận hàng (COD)', 'COD', N'Thanh toán bằng tiền mặt khi nhận hàng', 1, 1),
(N'Chuyển khoản QR Code', 'SEPAY_QR', N'Quét mã QR thanh toán qua BIDV. Tự động xác nhận trong 60s.', 1, 2);
GO

SELECT * FROM Payment_Methods;
```

---

## 📦 **3. REPOSITORIES**

### **PaymentRepository.java:**
```java
package com.example.demo.repository;

import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrder_Id(Integer orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}
```

### **PaymentMethodRepository.java:**
```java
package com.example.demo.repository;

import com.example.demo.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    Optional<PaymentMethod> findByMethodCode(String methodCode);
    List<PaymentMethod> findByIsActiveTrue();
}
```

---

## 🔑 **4. DTOs**

### **PaymentDTO.java:**
```java
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
```

### **QRPaymentRequestDTO.java:**
```java
package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QRPaymentRequestDTO {
    private String orderNumber;
    private BigDecimal amount;
    private String content; // UTEFASHION ORD20241234
}
```

---

## 🎯 **5. SEPAY SERVICE (QUAN TRỌNG NHẤT)**

### **SePayService.java:**
```java
package com.example.demo.service;

import com.example.demo.dto.SePayTransactionDTO;
import java.math.BigDecimal;

public interface SePayService {
    
    /**
     * Gọi SePay API để lấy danh sách transactions
     */
    SePayTransactionDTO getRecentTransactions(int limit);
    
    /**
     * Check xem có transaction nào match với order không
     * @param orderNumber - ORD-2024-1234
     * @param amount - Số tiền cần check
     * @param startTime - Thời điểm bắt đầu polling
     * @return Transaction nếu tìm thấy, null nếu không
     */
    SePayTransactionDTO.Transaction findMatchingTransaction(
        String orderNumber, 
        BigDecimal amount, 
        long startTime
    );
    
    /**
     * Generate QR Code URL
     * Format: https://img.vietqr.io/image/BIDV-96247NMB0A-compact2.png?amount=500000&addInfo=UTEFASHION%20ORD20241234
     */
    String generateQRCodeUrl(String orderNumber, BigDecimal amount);
}
```

### **SePayServiceImpl.java:** (FULL CODE)
```java
package com.example.demo.service.impl;

import com.example.demo.dto.SePayTransactionDTO;
import com.example.demo.service.SePayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            
            // Format: ORD-2024-1234 → ORD20241234 (để search)
            String searchContent = orderNumber.replace("-", "").toUpperCase();
            
            for (SePayTransactionDTO.Transaction tx : response.getTransactions()) {
                // Check amount
                if (tx.getAmountIn() == null || 
                    tx.getAmountIn().compareTo(amount) != 0) {
                    continue;
                }
                
                // Check content
                String content = tx.getTransactionContent();
                if (content == null || 
                    !content.toUpperCase().contains(searchContent)) {
                    continue;
                }
                
                // Check transaction time (phải sau startTime)
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime txTime = LocalDateTime.parse(tx.getTransactionDate(), formatter);
                    long txTimestamp = java.sql.Timestamp.valueOf(txTime).getTime();
                    
                    if (txTimestamp >= startTime) {
                        return tx; // ✅ FOUND!
                    }
                } catch (Exception e) {
                    // Ignore parse error
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
```

---

## 💼 **6. PAYMENT SERVICE**

### **PaymentService.java:**
```java
package com.example.demo.service;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.entity.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    
    List<PaymentMethod> getAllPaymentMethods();
    
    PaymentMethod getPaymentMethodByCode(String code);
    
    PaymentDTO createCODPayment(Integer orderId);
    
    PaymentDTO createSePayPayment(Integer orderId, String transactionId, String gatewayResponse);
    
    PaymentDTO getPaymentByOrderId(Integer orderId);
}
```

---

## 🎮 **7. PAYMENT CONTROLLER (CORE)**

### **PaymentController.java:**
```java
package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private SePayService sePayService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;

    /**
     * Trang chọn phương thức thanh toán
     */
    @GetMapping("/method-selection")
    public String showPaymentMethods(Model model, HttpSession session) {
        OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
        Integer userId = (Integer) session.getAttribute("checkoutUserId");
        
        if (checkoutData == null) {
            return "redirect:/cart";
        }
        
        // Lấy cart để hiển thị
        CartDTO cart = cartService.getCartByUserId(userId);
        
        // Lấy payment methods
        List<PaymentMethod> paymentMethods = paymentService.getAllPaymentMethods();
        
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutData", checkoutData);
        model.addAttribute("paymentMethods", paymentMethods);
        
        return "payment/method-selection";
    }

    /**
     * COD - Tạo order ngay
     */
    @PostMapping("/confirm-cod")
    public String confirmCOD(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
            Integer userId = (Integer) session.getAttribute("checkoutUserId");
            
            if (checkoutData == null) {
                return "redirect:/cart";
            }
            
            // ✅ TẠO ORDER
            OrderDTO createdOrder = orderService.createOrderFromCart(userId, session.getId(), checkoutData);
            
            // ✅ TẠO PAYMENT (COD - Pending)
            paymentService.createCODPayment(createdOrder.getId());
            
            // Xóa session
            session.removeAttribute("checkoutData");
            session.removeAttribute("checkoutUserId");
            
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Thanh toán khi nhận hàng.");
            return "redirect:/checkout/order-success?orderId=" + createdOrder.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/method-selection";
        }
    }

    /**
     * SePay QR - Hiển thị QR Code
     */
    @PostMapping("/sepay-qr/create")
    public String createSePayQR(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
            CartDTO cart = (CartDTO) session.getAttribute("cart");
            
            if (checkoutData == null) {
                return "redirect:/cart";
            }
            
            // ⭐ GENERATE ORDER NUMBER (chưa lưu DB)
            String orderNumber = "ORD-" + System.currentTimeMillis();
            BigDecimal amount = cart.getTotalAmount();
            
            // Lưu vào session
            session.setAttribute("pendingOrderNumber", orderNumber);
            session.setAttribute("pendingOrderAmount", amount);
            session.setAttribute("qrGeneratedAt", System.currentTimeMillis());
            
            // Generate QR URL
            String qrUrl = sePayService.generateQRCodeUrl(orderNumber, amount);
            
            model.addAttribute("orderNumber", orderNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("qrUrl", qrUrl);
            model.addAttribute("bankAccount", "96247NMB0A");
            model.addAttribute("bankName", "BIDV");
            model.addAttribute("content", "UTEFASHION " + orderNumber.replace("-", ""));
            
            return "payment/qr-payment";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/method-selection";
        }
    }

    /**
     * API Polling - Check transaction
     */
    @GetMapping("/sepay-qr/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSePayTransaction(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String orderNumber = (String) session.getAttribute("pendingOrderNumber");
            BigDecimal amount = (BigDecimal) session.getAttribute("pendingOrderAmount");
            Long startTime = (Long) session.getAttribute("qrGeneratedAt");
            
            if (orderNumber == null || startTime == null) {
                response.put("status", "error");
                response.put("message", "Session expired");
                return ResponseEntity.ok(response);
            }
            
            // Check timeout (60s)
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 60000) {
                response.put("status", "timeout");
                response.put("message", "QR code expired");
                return ResponseEntity.ok(response);
            }
            
            // ⭐ POLLING: Check SePay API
            SePayTransactionDTO.Transaction transaction = sePayService.findMatchingTransaction(
                orderNumber, 
                amount, 
                startTime
            );
            
            if (transaction != null) {
                // ✅ FOUND! Tạo order
                OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
                Integer userId = (Integer) session.getAttribute("checkoutUserId");
                
                // TẠO ORDER
                OrderDTO createdOrder = orderService.createOrderFromCart(userId, session.getId(), checkoutData);
                
                // TẠO PAYMENT (Success)
                paymentService.createSePayPayment(
                    createdOrder.getId(), 
                    transaction.getId().toString(),
                    transaction.toString()
                );
                
                // Update order payment status
                // orderService.updatePaymentStatus(createdOrder.getId(), "Paid");
                
                // Xóa session
                session.removeAttribute("checkoutData");
                session.removeAttribute("checkoutUserId");
                session.removeAttribute("pendingOrderNumber");
                session.removeAttribute("pendingOrderAmount");
                session.removeAttribute("qrGeneratedAt");
                
                response.put("status", "success");
                response.put("orderId", createdOrder.getId());
                response.put("transactionId", transaction.getId());
                
            } else {
                // Chưa tìm thấy
                response.put("status", "pending");
                response.put("message", "Waiting for payment...");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
```

---

## 🎨 **8. TEMPLATES**

### **payment/method-selection.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Chọn phương thức thanh toán</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="container mt-5">
        <h2 class="mb-4">Chọn phương thức thanh toán</h2>
        
        <!-- Order Summary -->
        <div class="alert alert-info">
            <h5>Thông tin đơn hàng</h5>
            <p>Tổng tiền: <strong th:text="${#numbers.formatDecimal(cart.totalAmount, 0, 'COMMA', 0, 'POINT')} + ' đ'"></strong></p>
        </div>

        <div class="row">
            <!-- COD -->
            <div class="col-md-6 mb-3" th:each="method : ${paymentMethods}">
                <div class="card h-100">
                    <div class="card-body">
                        <h5 class="card-title" th:text="${method.methodName}"></h5>
                        <p class="card-text" th:text="${method.description}"></p>
                        
                        <form th:if="${method.methodCode == 'COD'}" 
                              th:action="@{/payment/confirm-cod}" 
                              method="post">
                            <button type="submit" class="btn btn-primary">Chọn COD</button>
                        </form>
                        
                        <form th:if="${method.methodCode == 'SEPAY_QR'}" 
                              th:action="@{/payment/sepay-qr/create}" 
                              method="post">
                            <button type="submit" class="btn btn-success">Thanh toán QR</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

### **payment/qr-payment.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Thanh toán QR Code</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5 text-center">
        <h2>Quét mã QR để thanh toán</h2>
        
        <div class="card mt-4" style="max-width: 500px; margin: 0 auto;">
            <div class="card-body">
                <img th:src="${qrUrl}" alt="QR Code" class="img-fluid mb-3"/>
                
                <h4>Thông tin chuyển khoản:</h4>
                <p><strong>Ngân hàng:</strong> <span th:text="${bankName}"></span></p>
                <p><strong>Số tài khoản:</strong> <span th:text="${bankAccount}"></span></p>
                <p><strong>Số tiền:</strong> <span th:text="${#numbers.formatDecimal(amount, 0, 'COMMA', 0, 'POINT')} + ' đ'"></span></p>
                <p><strong>Nội dung:</strong> <span th:text="${content}"></span></p>
                
                <div id="status" class="alert alert-warning">
                    <i class="fas fa-spinner fa-spin"></i> Đang chờ thanh toán...
                </div>
                
                <div id="countdown" class="mt-3">
                    <strong>Thời gian còn lại: <span id="timer">60</span>s</strong>
                </div>
            </div>
        </div>
    </div>

    <script>
        let secondsLeft = 60;
        let pollingInterval;
        let countdownInterval;

        // Countdown timer
        countdownInterval = setInterval(() => {
            secondsLeft--;
            document.getElementById('timer').textContent = secondsLeft;
            
            if (secondsLeft <= 0) {
                clearInterval(countdownInterval);
                clearInterval(pollingInterval);
                document.getElementById('status').innerHTML = 
                    '<i class="fas fa-times-circle"></i> Hết thời gian! <a href="/payment/method-selection">Thử lại</a>';
                document.getElementById('status').className = 'alert alert-danger';
            }
        }, 1000);

        // Polling mỗi 3s
        pollingInterval = setInterval(async () => {
            try {
                const response = await fetch('/payment/sepay-qr/check');
                const data = await response.json();
                
                if (data.status === 'success') {
                    clearInterval(pollingInterval);
                    clearInterval(countdownInterval);
                    document.getElementById('status').innerHTML = 
                        '<i class="fas fa-check-circle"></i> Thanh toán thành công!';
                    document.getElementById('status').className = 'alert alert-success';
                    setTimeout(() => {
                        window.location.href = '/checkout/order-success?orderId=' + data.orderId;
                    }, 2000);
                } else if (data.status === 'timeout' || data.status === 'error') {
                    clearInterval(pollingInterval);
                    clearInterval(countdownInterval);
                    document.getElementById('status').innerHTML = 
                        '<i class="fas fa-times-circle"></i> ' + data.message;
                    document.getElementById('status').className = 'alert alert-danger';
                }
            } catch (error) {
                console.error('Polling error:', error);
            }
        }, 3000);
    </script>
</body>
</html>
```

---

## ✅ **CHECKLIST IMPLEMENTATION:**

```
☐ 1. Chạy Insert_Payment_Methods.sql
☐ 2. Thêm SePay config vào application.properties
☐ 3. Tạo Entity: Payment.java, PaymentMethod.java ✅
☐ 4. Tạo DTO: SePayTransactionDTO.java ✅, PaymentDTO, QRPaymentRequestDTO
☐ 5. Tạo Repository: PaymentRepository, PaymentMethodRepository
☐ 6. Tạo Service: SePayService, PaymentService
☐ 7. Tạo Impl: SePayServiceImpl, PaymentServiceImpl
☐ 8. Tạo Controller: PaymentController
☐ 9. Tạo Templates: method-selection.html, qr-payment.html
☐ 10. Test COD flow
☐ 11. Test SePay QR flow
```

---

## 🧪 **TEST FLOW:**

### **Test COD:**
```
1. Add sản phẩm vào cart
2. /checkout → Điền thông tin
3. /payment/method-selection → Chọn COD
4. Kiểm tra Order được tạo với payment_status = Unpaid
5. Kiểm tra Payment record với status = Pending
```

### **Test SePay QR:**
```
1. /payment/method-selection → Chọn QR
2. Hiển thị QR code
3. Chuyển khoản test với nội dung đúng
4. Sau 3-6s polling sẽ phát hiện
5. Order được tạo với payment_status = Paid
6. Payment record với status = Success
```

---

**DONE! Đây là toàn bộ hướng dẫn. Bạn muốn tôi tạo file nào trước?** 🚀
