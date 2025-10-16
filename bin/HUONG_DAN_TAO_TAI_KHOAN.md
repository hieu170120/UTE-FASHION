# HƯỚNG DẪN TẠO TÀI KHOẢN - UTE FASHION

## 🔐 TÀI KHOẢN MẪU

### **Sau khi chạy file SQL, bạn có thể đăng nhập với:**

| Username | Password | Role | Email |
|----------|----------|------|-------|
| admin | 123456 | ADMIN | admin@utefashion.com |
| manager1 | 123456 | MANAGER | manager@utefashion.com |
| user1 | 123456 | USER | user1@gmail.com |
| user2 | 123456 | USER | user2@gmail.com |
| user3 | 123456 | USER | user3@gmail.com |

---

## 📋 CÁCH 1: CHẠY SQL SCRIPT (KHUYẾN NGHỊ)

### **Bước 1: Mở SQL Server Management Studio**

### **Bước 2: Chạy file tạo database**
```sql
-- File: database/UTE_Fashion_Database_Schema.sql
-- Tạo database và tất cả các bảng
```

### **Bước 3: Chạy file insert dữ liệu mẫu**
```sql
-- File: database/UTE_Fashion_Sample_Data.sql
-- Insert tất cả dữ liệu mẫu bao gồm users, products, orders...
```

### **Bước 4: Kiểm tra**
```sql
USE UTE_Fashion;
SELECT * FROM Users;
```

Bạn sẽ thấy 5 users đã được tạo với password đã hash (BCrypt).

---

## 📋 CÁCH 2: ĐĂNG KÝ QUA WEBSITE

### **Bước 1: Truy cập trang đăng ký**
```
http://localhost:5055/UTE_Fashion/register
```

### **Bước 2: Điền form đăng ký**
- Tên đăng nhập: (tối thiểu 3 ký tự)
- Email: (phải hợp lệ)
- Họ và tên: (bắt buộc)
- Số điện thoại: (không bắt buộc)
- Mật khẩu: (tối thiểu 6 ký tự)
- Xác nhận mật khẩu: (phải khớp)

### **Bước 3: Click "Đăng ký"**

### **Bước 4: Đăng nhập**
```
http://localhost:5055/UTE_Fashion/login
```

---

## 📋 CÁCH 3: TẠO NHANH 1 TÀI KHOẢN TEST

Nếu bạn chưa chạy SQL và muốn test nhanh, chạy query này:

```sql
USE UTE_Fashion;

-- Tạo Role
INSERT INTO Roles (role_name, description) VALUES
('USER', N'Khách hàng');

-- Tạo User test
-- Password: "123456" -> BCrypt hash
INSERT INTO Users (username, email, password_hash, full_name, phone_number, is_active, is_email_verified) 
VALUES 
('testuser', 'test@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
 N'Test User', '0901234567', 1, 1);

-- Gán role cho user
INSERT INTO User_Roles (user_id, role_id) 
VALUES 
((SELECT user_id FROM Users WHERE username = 'testuser'), 
 (SELECT role_id FROM Roles WHERE role_name = 'USER'));
```

**Đăng nhập:**
- Username: `testuser`
- Password: `123456`

---

## 🔑 LƯU Ý VỀ PASSWORD

### **Password đã được mã hóa bằng BCrypt:**
```
Plain text: 123456
BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

### **Nếu muốn tạo password hash mới:**

**Cách 1: Dùng Online Tool**
- https://bcrypt-generator.com/
- Nhập password → Copy hash → Paste vào SQL

**Cách 2: Dùng Java Code**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println(hashedPassword);
    }
}
```

---

## 🚀 KIỂM TRA ỨNG DỤNG

### **1. Khởi động ứng dụng**
- Chạy `UteFashionApplication.java`
- Hoặc: `mvn spring-boot:run`

### **2. Truy cập các URL:**

**Trang chủ:**
```
http://localhost:5055/UTE_Fashion/
```

**Đăng nhập:**
```
http://localhost:5055/UTE_Fashion/login
```

**Đăng ký:**
```
http://localhost:5055/UTE_Fashion/register
```

### **3. Test đăng nhập:**
- Nhập username: `admin` (hoặc `user1`)
- Nhập password: `123456`
- Click "Đăng nhập"

### **4. Nếu thành công:**
- Redirect về trang chủ
- Hiển thị tên user ở góc phải navbar
- Có dropdown menu: Tài khoản, Đơn hàng, Đăng xuất

---

## ❌ XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi: "Tên đăng nhập hoặc mật khẩu không đúng"**
- ✅ Kiểm tra đã chạy SQL chưa
- ✅ Kiểm tra username và password
- ✅ Kiểm tra database connection trong `application.properties`

### **Lỗi: "Tài khoản đã bị khóa"**
- User có `is_active = 0`
- Sửa: 
```sql
UPDATE Users SET is_active = 1 WHERE username = 'admin';
```

### **Lỗi: Database connection failed**
- Kiểm tra SQL Server đang chạy
- Kiểm tra connection string trong `application.properties`
- Kiểm tra username/password SQL Server

---

## 📊 KIỂM TRA DATABASE

```sql
-- Kiểm tra users
SELECT user_id, username, email, full_name, is_active 
FROM Users;

-- Kiểm tra roles
SELECT u.username, r.role_name
FROM Users u
JOIN User_Roles ur ON u.user_id = ur.user_id
JOIN Roles r ON ur.role_id = r.role_id;

-- Kiểm tra password hash
SELECT username, password_hash 
FROM Users 
WHERE username = 'admin';
```

---

## 🎯 TỔNG KẾT

**Để đăng nhập thành công, bạn cần:**

1. ✅ Database `UTE_Fashion` đã được tạo
2. ✅ Tất cả bảng đã được tạo (chạy Schema SQL)
3. ✅ Dữ liệu mẫu đã được insert (chạy Sample Data SQL)
4. ✅ Application đang chạy trên port 5055
5. ✅ Truy cập đúng URL: `http://localhost:5055/UTE_Fashion/login`
6. ✅ Dùng username/password: `admin` / `123456`

---

**Chúc bạn thành công! 🎉**

