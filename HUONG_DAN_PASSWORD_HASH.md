# HƯỚNG DẪN ÁP DỤNG PASSWORD HASH - UTE FASHION

## 🔐 TỔNG QUAN

Dự án UTE Fashion đã được cập nhật để sử dụng **BCrypt password encoding** thay vì lưu password dưới dạng plain text. Điều này đảm bảo tính bảo mật cao cho hệ thống.

---

## 📋 CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1. **PasswordConfig.java** ✅
- **Trước:** Sử dụng `NoOpPasswordEncoder` (không mã hóa)
- **Sau:** Sử dụng `BCryptPasswordEncoder` (mã hóa an toàn)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 2. **AuthService.java** ✅
- **Đăng ký:** Password được mã hóa bằng BCrypt trước khi lưu vào DB
- **Đăng nhập:** Sử dụng `passwordEncoder.matches()` để so sánh password

```java
// Đăng ký - mã hóa password
user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

// Đăng nhập - so sánh password
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
}
```

### 3. **PasswordHashUpdater.java** ✅
- Utility class để cập nhật password hash cho các user hiện tại
- Chỉ chạy một lần để migrate từ plain text sang BCrypt

### 4. **PasswordMigrationController.java** ✅
- REST API để chạy migration
- **LƯU Ý:** Xóa controller này sau khi đã cập nhật xong!

---

## 🚀 CÁCH THỰC HIỆN MIGRATION

### **Bước 1: Cập nhật Database**
Chạy script SQL để cập nhật password hash cho user admin:

```sql
-- Cập nhật password hash cho user admin (password: admin123)
UPDATE Users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';
```

### **Bước 2: Restart Application**
```bash
# Dừng ứng dụng hiện tại (Ctrl+C)
# Chạy lại ứng dụng
mvn spring-boot:run
```

### **Bước 3: Test Đăng Nhập**
1. Truy cập: `http://localhost:5055/UTE_Fashion/login`
2. Đăng nhập với:
   - **Username:** `admin`
   - **Password:** `admin123`
3. Kiểm tra log để đảm bảo đăng nhập thành công

---

## 🔧 API MIGRATION (TÙY CHỌN)

Nếu muốn sử dụng API để migration:

### **Cập nhật tất cả user:**
```bash
POST http://localhost:5055/UTE_Fashion/api/admin/migrate-passwords
```

### **Cập nhật user cụ thể:**
```bash
POST http://localhost:5055/UTE_Fashion/api/admin/migrate-password/admin?password=admin123
```

---

## ✅ KIỂM TRA KẾT QUẢ

### **1. Kiểm tra Database**
```sql
SELECT user_id, username, email, password_hash, is_active 
FROM Users 
WHERE username = 'admin';
```

**Kết quả mong đợi:**
- `password_hash` phải bắt đầu bằng `$2a$10$` (BCrypt format)
- Không còn là plain text `admin123`

### **2. Kiểm tra Log**
Khi đăng nhập thành công, log sẽ hiển thị:
```
=== LOGIN SUCCESS ===
User logged in: admin
=== JWT TOKEN GENERATED ===
Token: eyJhbGciOiJIUzI1NiJ9...
```

### **3. Test Đăng Nhập**
- ✅ Đăng nhập với password đúng: Thành công
- ❌ Đăng nhập với password sai: Lỗi "Tên đăng nhập hoặc mật khẩu không đúng"

---

## 🛡️ BẢO MẬT

### **Ưu điểm của BCrypt:**
1. **Salt tự động:** Mỗi password có salt riêng
2. **Cost factor:** Có thể điều chỉnh độ khó (mặc định 10)
3. **Chống brute force:** Thời gian hash chậm
4. **Industry standard:** Được sử dụng rộng rãi

### **Ví dụ BCrypt hash:**
```
Password: admin123
Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**Giải thích:**
- `$2a$` - BCrypt algorithm version
- `10` - Cost factor (2^10 = 1024 rounds)
- `N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` - Salt + Hash

---

## 🚨 LƯU Ý QUAN TRỌNG

### **1. Backup Database**
Trước khi chạy migration, hãy backup database:
```sql
-- Backup table Users
SELECT * INTO Users_backup FROM Users;
```

### **2. Xóa Migration Controller**
Sau khi migration xong, **XÓA** `PasswordMigrationController.java`:
```bash
rm src/main/java/com/example/demo/controller/PasswordMigrationController.java
```

### **3. Test Kỹ Lưỡng**
- Test đăng nhập với tất cả user
- Test đăng ký user mới
- Test đổi mật khẩu (nếu có)

---

## 📝 CHECKLIST

- [ ] Cập nhật `PasswordConfig.java` ✅
- [ ] Cập nhật `AuthService.java` ✅
- [ ] Tạo `PasswordHashUpdater.java` ✅
- [ ] Tạo `PasswordMigrationController.java` ✅
- [ ] Tạo script SQL migration ✅
- [ ] Chạy script SQL migration
- [ ] Restart application
- [ ] Test đăng nhập
- [ ] Xóa migration controller
- [ ] Test đăng ký user mới

---

## 🎯 KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành migration:

1. ✅ **Tất cả password được mã hóa bằng BCrypt**
2. ✅ **Đăng nhập hoạt động bình thường**
3. ✅ **Đăng ký user mới tự động mã hóa password**
4. ✅ **Hệ thống bảo mật cao hơn**
5. ✅ **Tuân thủ best practices**

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề trong quá trình migration:

1. **Kiểm tra log** để xem lỗi cụ thể
2. **Rollback** bằng cách restore database backup
3. **Liên hệ team leader** để được hỗ trợ
4. **Tạo issue** trên GitHub với thông tin chi tiết

---

**🎉 Chúc mừng! Hệ thống UTE Fashion giờ đã có password security cao cấp!**


