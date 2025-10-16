# 💳 PAYMENT MODULE - HƯỚNG DẪN SETUP

## 📋 **MỤC LỤC**

1. [Tổng quan](#tổng-quan)
2. [Chuẩn bị Database](#chuẩn-bị-database)
3. [Kiểm tra Backend Code](#kiểm-tra-backend-code)
4. [Testing](#testing)
5. [Troubleshooting](#troubleshooting)

---

## 🎯 **TỔNG QUAN**

Payment Module hỗ trợ 2 phương thức thanh toán:
- **COD (Cash on Delivery):** Thanh toán khi nhận hàng
- **SePay QR:** Thanh toán qua QR Code banking với auto-verify

### **Files đã tạo:**

#### **Backend (Java):**
```
src/main/java/com/example/demo/
├── entity/
│   ├── Payment.java ✅
│   └── PaymentMethod.java ✅
├── dto/
│   ├── SePayTransactionDTO.java ✅
│   ├── PaymentDTO.java ✅
│   └── QRPaymentRequestDTO.java ✅
├── repository/
│   ├── PaymentRepository.java ✅
│   └── PaymentMethodRepository.java ✅
├── service/
│   ├── PaymentService.java ✅
│   ├── SePayService.java ✅
│   └── impl/
│       ├── PaymentServiceImpl.java ✅
│       └── SePayServiceImpl.java ✅
├── controller/
│   └── PaymentController.java ✅
└── config/
    └── SePayConfig.java ✅
```

#### **Frontend (HTML/Thymeleaf):**
```
src/main/resources/templates/payment/
├── method-selection.html ✅
└── qr-payment.html ✅
```

#### **Database (SQL):**
```
database/
├── Add_Payment_Tables.sql ✅ (Tạo bảng)
└── Insert_Payment_Methods.sql ✅ (Insert dữ liệu)
```

#### **Configuration:**
```
src/main/resources/
└── application.properties ✅ (Updated with SePay config)
```

---

## 🗄️ **CHUẨN BỊ DATABASE**

### **BƯỚC 1: Tạo bảng Payment_Methods và Payments**

**Mở SQL Server Management Studio và chạy:**

```sql
-- File: database/Add_Payment_Tables.sql
```

**Hoặc copy script này:**

```sql
USE UTE_Fashion;
GO

-- Tạo bảng Payment_Methods
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payment_Methods]') AND type in (N'U'))
BEGIN
    CREATE TABLE Payment_Methods (
        payment_method_id INT PRIMARY KEY IDENTITY(1,1),
        method_name NVARCHAR(100) NOT NULL UNIQUE,
        method_code NVARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(500),
        icon_url NVARCHAR(500),
        is_active BIT DEFAULT 1,
        display_order INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );
    PRINT '✅ Đã tạo bảng Payment_Methods';
END
GO

-- Tạo bảng Payments
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payments]') AND type in (N'U'))
BEGIN
    CREATE TABLE Payments (
        payment_id INT PRIMARY KEY IDENTITY(1,1),
        order_id INT NOT NULL,
        payment_method_id INT NOT NULL,
        transaction_id NVARCHAR(255),
        amount DECIMAL(18,2) NOT NULL,
        payment_status NVARCHAR(50) NOT NULL DEFAULT 'Pending',
        payment_gateway_response NVARCHAR(MAX),
        paid_at DATETIME,
        refunded_at DATETIME,
        refund_amount DECIMAL(18,2),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
        FOREIGN KEY (payment_method_id) REFERENCES Payment_Methods(payment_method_id)
    );
    PRINT '✅ Đã tạo bảng Payments';
END
GO
```

**Expected Output:**
```
✅ Đã tạo bảng Payment_Methods
✅ Đã tạo bảng Payments
```

---

### **BƯỚC 2: Insert Payment Methods**

**Chạy script:**

```sql
-- File: database/Insert_Payment_Methods.sql
```

**Hoặc copy:**

```sql
USE UTE_Fashion;
GO

-- Xóa dữ liệu cũ
DELETE FROM Payment_Methods;
GO

-- Insert COD
INSERT INTO Payment_Methods (method_name, method_code, description, is_active, display_order) 
VALUES (
    N'Thanh toán khi nhận hàng (COD)',
    'COD',
    N'Thanh toán bằng tiền mặt khi nhận hàng. Bạn sẽ trả tiền trực tiếp cho nhân viên giao hàng.',
    1,
    1
);

-- Insert SePay QR
INSERT INTO Payment_Methods (method_name, method_code, description, is_active, display_order) 
VALUES (
    N'Chuyển khoản QR Code',
    'SEPAY_QR',
    N'Quét mã QR để thanh toán qua ngân hàng BIDV. Đơn hàng sẽ được tự động xác nhận trong vòng 60 giây.',
    1,
    2
);
GO

-- Kiểm tra
SELECT * FROM Payment_Methods ORDER BY display_order;
GO
```

**Expected Output:**
```
payment_method_id | method_name                        | method_code | is_active
------------------|---------------------------------------|-------------|----------
1                 | Thanh toán khi nhận hàng (COD)      | COD         | 1
2                 | Chuyển khoản QR Code                | SEPAY_QR    | 1
```

---

## ✅ **KIỂM TRA BACKEND CODE**

### **BƯỚC 3: Verify Configuration**

**Kiểm tra `application.properties`:**

```properties
# SePay Configuration (đã được thêm)
sepay.api.url=https://my.sepay.vn/userapi
sepay.api.key=POP0SR3X7EEE0DYHSJKM1MHJBGFEMTUL4IIQLSNNIPZU59WPRNAAFVQWQZ8QWBJC
sepay.bank.account=96247NMB0A
sepay.bank.name=BIDV
sepay.polling.interval=3000
sepay.polling.timeout=60000
```

---

### **BƯỚC 4: Build và Run Application**

```bash
# Clean và build
mvn clean install

# Run application
mvn spring-boot:run
```

**Hoặc từ IDE:**
- Right-click → Run `UTE-FASHION Application`

**Check console output:**
```
Started UTE-FASHION Application on port 5055
Context path: /UTE_Fashion
```

---

## 🧪 **TESTING**

### **Test 1: COD Flow**

1. **Add sản phẩm vào cart**
   ```
   http://localhost:5055/UTE_Fashion/products
   → Click "Thêm vào giỏ"
   ```

2. **Checkout**
   ```
   http://localhost:5055/UTE_Fashion/checkout
   → Điền thông tin giao hàng
   → Click "Đặt hàng"
   ```

3. **Chọn COD**
   ```
   http://localhost:5055/UTE_Fashion/payment/method-selection
   → Click "Chọn COD"
   ```

4. **Verify trong Database**
   ```sql
   -- Kiểm tra Order
   SELECT TOP 1 
       order_number, 
       payment_status,  -- Should be 'Unpaid'
       order_status     -- Should be 'Pending'
   FROM Orders 
   ORDER BY created_at DESC;

   -- Kiểm tra Payment
   SELECT TOP 1 
       transaction_id,  -- Should be 'COD-ORD-...'
       payment_status,  -- Should be 'Pending'
       amount
   FROM Payments 
   ORDER BY created_at DESC;
   ```

**Expected Results:**
- ✅ Order created với `payment_status = 'Unpaid'`
- ✅ Payment created với `payment_status = 'Pending'`
- ✅ Cart cleared
- ✅ Redirect to success page

---

### **Test 2: SePay QR Flow (Mock Test)**

1. **Add sản phẩm vào cart và checkout** (giống Test 1)

2. **Chọn SePay QR**
   ```
   http://localhost:5055/UTE_Fashion/payment/method-selection
   → Click "Thanh toán QR"
   ```

3. **QR Page hiển thị**
   ```
   - QR Code image từ VietQR.io
   - Thông tin: Bank, Account, Amount, Content
   - Countdown: 60s
   - Status: "Đang chờ thanh toán..."
   ```

4. **Polling bắt đầu**
   ```
   - JavaScript gọi /payment/sepay-qr/check mỗi 3s
   - Backend gọi SePay API để check transaction
   ```

5. **Mock Success (để test frontend)**
   - Nếu muốn test UI, có thể modify controller tạm thời:
   ```java
   // PaymentController.java - Line 188 (temporary)
   if (transaction != null) {  // Change to: if (true) {
   ```

**Expected Results:**
- ✅ QR code displays correctly
- ✅ Countdown works (60 → 0)
- ✅ Polling calls API every 3s
- ✅ On timeout: Shows error message
- ✅ Cart remains if payment fails

---

### **Test 3: API Endpoints**

**Test Payment Method Selection:**
```bash
curl http://localhost:5055/UTE_Fashion/payment/method-selection
# Should return HTML with payment methods
```

**Test Polling Endpoint:**
```bash
curl http://localhost:5055/UTE_Fashion/payment/sepay-qr/check
# Should return JSON:
# {"status":"error","message":"Session expired"}
```

---

## 🔧 **TROUBLESHOOTING**

### **Lỗi 1: "Invalid object name 'Payment_Methods'"**

**Nguyên nhân:** Bảng chưa được tạo

**Giải pháp:**
```sql
-- Chạy Add_Payment_Tables.sql trước
-- Sau đó chạy Insert_Payment_Methods.sql
```

---

### **Lỗi 2: "Payment method not found: COD"**

**Nguyên nhân:** Chưa insert payment methods

**Giải pháp:**
```sql
-- Chạy Insert_Payment_Methods.sql
SELECT * FROM Payment_Methods; -- Verify
```

---

### **Lỗi 3: "404 Not Found - /payment/method-selection"**

**Nguyên nhân:** Context path không đúng

**Giải pháp:**
```
URL đúng: http://localhost:5055/UTE_Fashion/payment/method-selection
                                 ^^^^^^^^^^ Context path
```

---

### **Lỗi 4: QR Code không hiển thị**

**Nguyên nhân:** 
- Cart trống
- Session expired

**Giải pháp:**
1. Kiểm tra cart có items
2. Đảm bảo đã qua checkout trước
3. Check session timeout (30 minutes)

---

### **Lỗi 5: Polling không hoạt động**

**Nguyên nhân:** JavaScript error hoặc CORS

**Giải pháp:**
1. Open browser console (F12)
2. Check for JavaScript errors
3. Verify polling calls in Network tab
4. Should see GET requests every 3s

---

## 📊 **CHECKLIST HOÀN THÀNH**

### **Database:**
- [ ] ✅ Chạy `Add_Payment_Tables.sql` thành công
- [ ] ✅ Chạy `Insert_Payment_Methods.sql` thành công
- [ ] ✅ Verify: `SELECT * FROM Payment_Methods` trả về 2 rows

### **Backend:**
- [ ] ✅ Application build successful (no errors)
- [ ] ✅ Application starts on port 5055
- [ ] ✅ No bean creation errors in console

### **Frontend:**
- [ ] ✅ `/payment/method-selection` accessible
- [ ] ✅ Payment methods display correctly
- [ ] ✅ `/payment/sepay-qr/create` generates QR code

### **Integration:**
- [ ] ✅ COD flow works end-to-end
- [ ] ✅ QR page displays with countdown
- [ ] ✅ Polling works (check Network tab)

---

## 🎯 **NEXT STEPS**

### **Production Ready:**

1. **SePay API Key:**
   - Replace test key với production key
   - Get key từ: https://my.sepay.vn

2. **Error Handling:**
   - Add proper error pages
   - Log exceptions
   - User-friendly error messages

3. **Security:**
   - Validate all inputs
   - CSRF protection
   - Rate limiting cho polling

4. **Monitoring:**
   - Log payment transactions
   - Track success/failure rates
   - Monitor SePay API response times

---

## 📞 **HỖ TRỢ**

Nếu gặp vấn đề:
1. Check logs: `logs/spring.log`
2. Check database: Verify tables exist
3. Check browser console: For JS errors
4. Check network tab: For API calls

---

**DONE!** 🎉

Module thanh toán đã sẵn sàng!
