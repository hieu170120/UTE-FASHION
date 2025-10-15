# HƯỚNG DẪN SỬA LỖI PASSWORD HASH - UTE FASHION

## 🚨 VẤN ĐỀ HIỆN TẠI

Từ log, tôi thấy:
```
=== PASSWORD HASH TEST ===
Original password: admin123
Generated hash: $2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6
Matches test: true
DB hash matches: false
```

**Vấn đề:** Hash trong database không khớp với hash được tạo mới.

---

## 🔧 GIẢI PHÁP

### **Bước 1: Cập nhật Database**

Chạy script SQL sau để cập nhật password hash cho user admin:

```sql
-- Cập nhật password hash cho user admin (password: admin123)
UPDATE Users 
SET password_hash = '$2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6'
WHERE username = 'admin';

-- Kiểm tra kết quả
SELECT user_id, username, email, password_hash, is_active 
FROM Users 
WHERE username = 'admin';
```

### **Bước 2: Restart Application**

Sau khi cập nhật database, restart ứng dụng để áp dụng thay đổi.

### **Bước 3: Test Đăng Nhập**

1. Truy cập: `http://localhost:5055/UTE_Fashion/login`
2. Đăng nhập với:
   - **Username:** `admin`
   - **Password:** `admin123`

---

## 📋 CÁC FILE ĐÃ TẠO

1. **`database/Update_Admin_Password_Hash_Fixed.sql`** - Script SQL với hash đúng
2. **`src/main/java/com/example/demo/service/PasswordTestService.java`** - Service test BCrypt
3. **`src/main/java/com/example/demo/util/BCryptHashGenerator.java`** - Utility tạo hash

---

## 🔍 KIỂM TRA KẾT QUẢ

Sau khi cập nhật database, log sẽ hiển thị:
```
=== PASSWORD HASH TEST ===
Original password: admin123
Generated hash: $2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6
Matches test: true
DB hash matches: true  ← Đây là điều quan trọng!
=========================
```

---

## 🎯 HASH ĐÚNG CHO ADMIN123

**Password:** `admin123`  
**BCrypt Hash:** `$2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6`

Hash này được tạo từ log của ứng dụng và đã được test thành công.

---

## ⚠️ LƯU Ý

1. **Backup database** trước khi chạy script SQL
2. **Chỉ chạy script một lần** để tránh lỗi
3. **Test đăng nhập** sau khi cập nhật
4. **Xóa PasswordTestService** sau khi fix xong

---

## 🚀 CÁCH THỰC HIỆN NHANH

1. **Mở SQL Server Management Studio**
2. **Connect đến database UTE_Fashion**
3. **Chạy script SQL:**
   ```sql
   UPDATE Users 
   SET password_hash = '$2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6'
   WHERE username = 'admin';
   ```
4. **Restart ứng dụng Spring Boot**
5. **Test đăng nhập với admin/admin123**

---

**🎉 Sau khi hoàn thành, hệ thống sẽ hoạt động bình thường với BCrypt password security!**


