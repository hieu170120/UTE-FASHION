# HƯỚNG DẪN AUTO-REFRESH SESSION XU

## 📋 Vấn Đề

Sau khi vendor approve return request hoặc các action khác hoàn xu cho customer, số xu hiển thị trên navbar KHÔNG được cập nhật cho đến khi customer login lại.

**Nguyên nhân**: Session user không được refresh sau khi xu thay đổi trong database.

---

## ✅ Giải Pháp

Thêm **auto-refresh session user** vào các trang mà customer thường xem sau khi có thay đổi về xu:

### 1. **Trang Danh Sách Đơn Hàng** (`/orders/my-orders`)

```java
// ✅ REFRESH SESSION USER - Cập nhật số xu từ database
User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
session.setAttribute("currentUser", updatedUser);
currentUser = updatedUser;
```

### 2. **Trang Chi Tiết Đơn Hàng** (`/orders/{orderId}/detail`)

```java
// ✅ REFRESH SESSION USER - Cập nhật số xu từ database
User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
session.setAttribute("currentUser", updatedUser);
currentUser = updatedUser;
```

### 3. **Trang Profile** (`/profile`)

```java
// ✅ REFRESH SESSION USER - Cập nhật số xu từ database
HttpSession session = request.getSession(false);
if (session != null) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser != null) {
        User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
        session.setAttribute("currentUser", updatedUser);
    }
}
```

### 4. **Trang Giỏ Hàng** (`/cart`)

```java
// ✅ REFRESH SESSION USER - Cập nhật số xu từ database
if (currentUser != null) {
    User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
    session.setAttribute("currentUser", updatedUser);
    currentUser = updatedUser;
}
```

### 5. **Trang Checkout** (`/checkout`)

```java
// ✅ REFRESH SESSION USER - Cập nhật số xu từ database
User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
session.setAttribute("currentUser", updatedUser);
currentUser = updatedUser;
```

---

## 📊 Flow Hoàn Xu & Refresh Session

### Flow 1: Vendor Approve Return Request

```
Customer đặt hàng → Nhận hàng → Yêu cầu trả hàng
    ↓
Vendor approve return request
    ↓
🔄 refundCoinsIfEligible() → Hoàn xu vào database
    ↓
❌ Session của customer CHƯA được update (vì đang ở vendor session)
    ↓
Customer vào /orders/my-orders hoặc /orders/{orderId}/detail
    ↓
✅ Auto-refresh session user → Số xu hiển thị đúng trên navbar!
```

### Flow 2: Customer Hủy Đơn (đã thanh toán QR/Coin)

```
Customer bấm "Hủy đơn" → POST /orders/{orderId}/cancel
    ↓
orderManagementService.customerCancelOrder()
    ↓
refundCoinsIfEligible() → Hoàn xu vào database
    ↓
✅ CustomerOrderController.cancelOrder() → Refresh session ngay!
    ↓
Redirect về /orders/{orderId}/detail
    ↓
✅ Refresh session lại lần nữa → Đảm bảo xu hiển thị đúng!
```

### Flow 3: Thanh Toán Bằng Xu

```
Customer chọn "Thanh toán bằng xu" → POST /payment/confirm-coin
    ↓
paymentService.createCoinPayment() → Trừ xu trong database
    ↓
✅ PaymentController.confirmCoin() → Refresh session ngay!
    ↓
Redirect về /checkout/order-success
    ↓
✅ Số xu hiển thị đúng trên navbar!
```

---

## 📁 Files Đã Chỉnh Sửa

### Backend Controllers

#### 1. `src/main/java/com/example/demo/controller/CustomerOrderController.java`

**Thay đổi:**

- Thêm refresh session trong `myOrders()` (line 50-53)
- Thêm refresh session trong `orderDetail()` (line 134-137)
- Xóa unused import `org.springframework.data.domain.Sort`

**Mục đích:** Cập nhật xu khi customer xem đơn hàng (sau khi vendor approve return)

---

#### 2. `src/main/java/com/example/demo/controller/ProfileController.java`

**Thay đổi:**

- Thêm `@Autowired UserRepository userRepository` (line 38-39)
- Thêm refresh session trong `profilePage()` (line 63-71)

**Mục đích:** Cập nhật xu khi customer vào trang profile

---

#### 3. `src/main/java/com/example/demo/controller/CartController.java`

**Thay đổi:**

- Thêm `import UserRepository` (line 6)
- Thêm `@Autowired UserRepository userRepository` (line 24-25)
- Thêm refresh session trong `viewCart()` (line 31-36)

**Mục đích:** Cập nhật xu khi customer xem giỏ hàng

---

#### 4. `src/main/java/com/example/demo/controller/CheckoutController.java`

**Thay đổi:**

- Thêm refresh session trong `showCheckoutPage()` (line 84-87)
- Xóa unused imports

**Mục đích:** Cập nhật xu khi customer vào trang checkout

---

#### 5. `src/main/java/com/example/demo/controller/PaymentController.java`

**Đã có sẵn** (từ lần implement trước):

- Refresh session trong `confirmCoin()` sau khi thanh toán bằng xu

---

## 🎯 Kết Quả

### ✅ Trước đây:

- Hoàn xu thành công trong database ✅
- Nhưng navbar vẫn hiển thị số xu cũ ❌
- Phải login lại mới cập nhật ❌

### ✅ Bây giờ:

- Hoàn xu thành công trong database ✅
- Navbar tự động cập nhật số xu mới ✅
- Không cần login lại ✅

---

## 🔍 Test Cases

### Test 1: Vendor Approve Return

1. Customer đặt hàng và thanh toán bằng QR/Coin (xu bị trừ)
2. Đơn hàng delivered
3. Customer yêu cầu trả hàng
4. Vendor approve return request
5. **✅ Kiểm tra:** Customer vào `/orders/my-orders` → Số xu đã được hoàn và hiển thị đúng trên navbar

### Test 2: Customer Hủy Đơn

1. Customer đặt hàng và thanh toán bằng QR/Coin (xu bị trừ)
2. Đơn chưa được vendor xác nhận (status = PROCESSING)
3. Customer bấm "Hủy đơn"
4. **✅ Kiểm tra:** Sau khi hủy → Số xu được hoàn và hiển thị ngay trên navbar

### Test 3: Thanh Toán Bằng Xu

1. Customer có 100,000 xu
2. Đặt hàng 50,000 xu và chọn "Thanh toán bằng xu"
3. Confirm payment
4. **✅ Kiểm tra:** Navbar hiển thị 50,000 xu còn lại ngay lập tức

---

## 📝 Lưu Ý

### Performance

- Mỗi lần refresh session chỉ query 1 lần database (`userRepository.findById()`)
- Query theo Primary Key → rất nhanh
- Chỉ refresh khi user vào trang → không ảnh hưởng performance

### Alternative Solutions (không dùng)

1. **WebSocket** - Quá phức tạp cho yêu cầu đơn giản này
2. **Polling** - Tốn resource, không cần thiết
3. **Interceptor** - Refresh mọi request → tốn resource không cần thiết
4. **Filter** - Tương tự interceptor

**Giải pháp hiện tại (selective refresh)** là tối ưu nhất: đơn giản, hiệu quả, và chỉ refresh khi cần.

---

## 🚀 Deploy

Sau khi update code:

1. Build lại project: `mvn clean install`
2. Restart Spring Boot application
3. Test các flow trên
4. ✅ Done!

