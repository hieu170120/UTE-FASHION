# 🚀 HƯỚNG DẪN SETUP DỰ ÁN UTE FASHION

## ⚡ QUICK START - CLONE VÀ CHẠY NGAY!

### **Bước 1: Clone project**
```bash
git clone https://github.com/hieu170120/UTE-FASHION.git
cd UTE-FASHION
```

### **Bước 2: Chạy application**
```bash
mvn spring-boot:run
```

### **Bước 3: Truy cập**
```
http://localhost:5055/UTE_Fashion
```

### **Bước 4: Đăng nhập**
```
Username: admin
Password: 123456
```

**ĐÓ LÀ TẤT CẢ!** ✅ Không cần config gì thêm!

---

## 🎯 TẠI SAO ĐƠN GIẢN VẬY?

- ✅ **Database đã config sẵn** trong `application.properties`
- ✅ **AWS RDS** - Database chung của cả team
- ✅ **Sample data** đã có sẵn (users, products, orders)
- ✅ **Clone về là chạy được luôn!**

---

## 🖥️ CHẠY TRONG ECLIPSE/SPRING TOOL SUITE

### **Cách 1: Run As Spring Boot App**
1. Right-click vào file `UteFashionApplication.java`
2. **Run As** → **Spring Boot App**
3. Xong! ✅

### **Cách 2: Run Maven**
1. Right-click vào project
2. **Run As** → **Maven build...**
3. Goals: `spring-boot:run`
4. **Run**

---

## 💾 DATABASE CHUNG - AWS RDS

### **Thông tin:**
- **Endpoint:** ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com
- **Database:** UTE_Fashion
- **Tất cả dùng chung 1 database** trên AWS

### **Lợi ích:**
- ✅ Không cần cài SQL Server trên máy
- ✅ Data đồng bộ giữa các thành viên
- ✅ Làm việc mọi lúc mọi nơi (có internet)
- ✅ Xem được data của nhau real-time

### **Tài khoản test có sẵn:**
```
Admin:
  Username: admin
  Password: 123456

User:
  Username: user1
  Password: 123456
```

---

## 📊 XEM DATABASE TRỰC TIẾP

### **Sử dụng SQL Server Management Studio (SSMS):**

**Connect to Server:**
```
Server name: ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com,1433
Authentication: SQL Server Authentication
Login: admin
Password: YourStrongPassword123!
```

**Xem tables:**
- Expand **Databases** → **UTE_Fashion** → **Tables**
- Có 30 bảng: Users, Products, Orders, Payments...

---

## 🌿 GIT WORKFLOW

### **Mỗi người làm trên branch riêng:**

**Người 1: Authentication Module**
```bash
git checkout -b feature/auth-module
# Code...
git add .
git commit -m "feat: add login feature"
git push origin feature/auth-module
```

**Người 2: Product Module**
```bash
git checkout -b feature/product-module
# Code...
git push origin feature/product-module
```

**Người 3: Order Module**
```bash
git checkout -b feature/order-module
# Code...
git push origin feature/order-module
```

**Người 4: Payment Module**
```bash
git checkout -b feature/payment-module
# Code...
git push origin feature/payment-module
```

### **Quy tắc:**
- ❌ Không commit trực tiếp vào `main`
- ✅ Tạo Pull Request để merge
- ✅ Team Leader review trước khi merge

---

## 🧪 TEST ỨNG DỤNG

### **1. Test đăng ký:**
```
http://localhost:5055/UTE_Fashion/register
```
- Điền form đăng ký
- Submit
- Check database: SELECT * FROM Users;

### **2. Test đăng nhập:**
```
http://localhost:5055/UTE_Fashion/login
```
- Username: admin
- Password: 123456

### **3. Xem data:**
```sql
-- Xem tất cả users
SELECT * FROM Users;

-- Xem products
SELECT * FROM Products;

-- Xem orders
SELECT * FROM Orders;
```

---

## 🐛 XỬ LÝ LỖI

### **Lỗi: Connection timeout**
```
Error: Connection is not available
```
**Giải pháp:**
- Kiểm tra internet
- AWS RDS có thể đang maintenance
- Hỏi Team Leader

### **Lỗi: Port 5055 already in use**
```
Error: Port 5055 is already in use
```
**Giải pháp:**
- Tắt application đang chạy
- Hoặc đổi port trong `application.properties`:
```properties
server.port=8080
```

### **Lỗi: Database không tồn tại**
```
Error: Cannot open database "UTE_Fashion"
```
**Giải pháp:**
- Liên hệ Team Leader
- Database có thể chưa được tạo

---

## 📦 DEPENDENCIES

Project sử dụng:
- ✅ Spring Boot 3.2.0
- ✅ Spring Data JPA
- ✅ Spring Security
- ✅ Thymeleaf
- ✅ Bootstrap 5
- ✅ SQL Server JDBC Driver
- ✅ JWT (JSON Web Token)
- ✅ WebSocket
- ✅ Lombok

Tất cả đã config sẵn trong `pom.xml`

---

## 📁 CẤU TRÚC PROJECT

```
UTE-Fashion/
├── src/main/java/com/example/demo/
│   ├── entity/          # User, Product, Order...
│   ├── repository/      # JPA Repositories
│   ├── service/         # Business Logic
│   ├── controller/      # REST APIs & Pages
│   ├── dto/             # Data Transfer Objects
│   └── config/          # Security Config
├── src/main/resources/
│   ├── application.properties    # Config chính
│   ├── templates/       # Thymeleaf HTML
│   └── static/          # CSS, JS, Images
├── database/
│   ├── UTE_Fashion_Database_Schema.sql
│   └── UTE_Fashion_Sample_Data.sql
└── pom.xml
```

---

## 👥 PHÂN CÔNG MODULE

| Người | Module | Branch |
|-------|--------|--------|
| Người 1 | Authentication & User | `feature/auth-module` |
| Người 2 | Product & Category | `feature/product-module` |
| Người 3 | Cart & Order | `feature/order-module` |
| Người 4 | Payment & Reports | `feature/payment-module` |

---

## 📞 HỖ TRỢ

**Khi gặp vấn đề:**
1. Đọc lại hướng dẫn này
2. Hỏi trong group chat
3. Liên hệ Team Leader (Người 1)
4. Tạo Issue trên GitHub

---

## ✅ CHECKLIST

- [ ] Clone project từ Git
- [ ] Chạy `mvn spring-boot:run`
- [ ] Thấy log "UTE Fashion Application Started!"
- [ ] Truy cập http://localhost:5055/UTE_Fashion
- [ ] Đăng nhập thành công
- [ ] Tạo branch cho module của mình
- [ ] Bắt đầu code

---

## 🎉 TIPS

**Tip 1: Hot Reload**
- Dùng Spring Boot DevTools
- Sửa code → Tự động restart

**Tip 2: Xem logs**
```bash
# Hibernate sẽ show SQL queries
Hibernate: select user0_.user_id as user_id1_...
```

**Tip 3: Debug**
- Đặt breakpoint trong Eclipse
- Run as Debug mode
- F5: Step into, F6: Step over

**Tip 4: Clean & Build**
```bash
mvn clean install
```

---

**Chúc các bạn code vui vẻ! 🚀**

**Mọi thắc mắc hỏi Team Leader hoặc group chat!** 💬

