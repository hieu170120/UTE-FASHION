# 📋 HƯỚNG DẪN SETUP DATABASE CHO TEAM UTE FASHION

## 🎯 TỔNG QUAN

Team sử dụng **AWS RDS SQL Server** làm database chung để tất cả mọi người có thể làm việc cùng nhau.

**Lợi ích:**
- ✅ Tất cả cùng dùng 1 database
- ✅ Không cần setup SQL Server trên máy local
- ✅ Data đồng bộ giữa các thành viên
- ✅ Truy cập từ mọi nơi có internet

---

## 📦 CẤU TRÚC FILE CẤU HÌNH

```
src/main/resources/
├── application.properties              # File gốc (được commit)
├── application-local.properties.example    # Template cho local (được commit)
├── application-aws.properties.example      # Template cho AWS (được commit)
├── application-local.properties           # Config local của bạn (KHÔNG commit)
└── application-aws.properties             # Config AWS thực tế (KHÔNG commit)
```

**Quy tắc:**
- ✅ File `.example` được commit lên Git (là template)
- ❌ File không có `.example` KHÔNG được commit (chứa password)

---

## 🚀 SETUP CHO TỪNG THÀNH VIÊN

### **📍 BƯỚC 1: Clone Project**

```bash
git clone https://github.com/hieu170120/UTE-FASHION.git
cd UTE-FASHION
```

### **📍 BƯỚC 2: Tạo File Cấu Hình AWS**

**2.1. Copy template:**
```bash
# Windows
copy src\main\resources\application-aws.properties.example src\main\resources\application-aws.properties

# macOS/Linux
cp src/main/resources/application-aws.properties.example src/main/resources/application-aws.properties
```

**2.2. Lấy thông tin AWS RDS từ Team Leader**

Liên hệ **Team Leader (Người 1)** để lấy thông tin:
- AWS RDS Endpoint
- Username
- Password
- Database name

**2.3. Mở file `application-aws.properties` và điền thông tin:**

```properties
# Thay YOUR_AWS_RDS_ENDPOINT bằng endpoint thực tế
spring.datasource.url=jdbc:sqlserver://ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
spring.datasource.username=admin
spring.datasource.password=YourStrongPassword123!
```

### **📍 BƯỚC 3: Chạy Application với AWS Profile**

**Option 1: Maven Command**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

**Option 2: Eclipse/Spring Tool Suite**
- Right-click project → **Run As** → **Run Configurations**
- Tab **Arguments**
- **VM arguments:** `-Dspring.profiles.active=aws`
- Click **Apply** → **Run**

**Option 3: IntelliJ IDEA**
- **Run** → **Edit Configurations**
- **Environment variables:** `SPRING_PROFILES_ACTIVE=aws`
- Click **OK** → **Run**

### **📍 BƯỚC 4: Kiểm Tra Kết Nối**

Khi application chạy, xem logs:
```
Hibernate: 
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
UTE Fashion Application Started!
Access: http://localhost:5055/UTE_Fashion
```

Nếu thấy dòng trên → **Kết nối thành công!** ✅

---

## 🔐 THÔNG TIN AWS RDS (CHỈ CHO TEAM)

### **AWS RDS Connection Info:**

```
Endpoint: ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com
Port: 1433
Database: UTE_Fashion
Username: admin
Password: [Hỏi Team Leader]

Full Connection String:
jdbc:sqlserver://ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
```

⚠️ **LƯU Ý:** Không share password công khai, chỉ gửi qua Zalo/Discord riêng tư!

---

## 🔧 TEST KẾT NỐI BẰNG SQL SERVER MANAGEMENT STUDIO

Nếu bạn muốn xem database trực tiếp:

**Bước 1: Mở SSMS**

**Bước 2: Connect to Server**
```
Server type: Database Engine
Server name: ute-fashion-db.c05qaoqgqm5q.us-east-1.rds.amazonaws.com,1433
Authentication: SQL Server Authentication
Login: admin
Password: [Hỏi Team Leader]
```

**Bước 3: Click Connect**

**Bước 4: Xem database**
- Expand **Databases**
- Expand **UTE_Fashion**
- Expand **Tables**
- Bạn sẽ thấy: Users, Products, Orders, Payments...

---

## 👥 PHÂN CÔNG VÀ WORKFLOW

### **Người 1: Team Leader + Auth Module**
- Quản lý AWS RDS
- Phân phối credentials cho team
- Module: Authentication

### **Người 2: Product Module**
- Sử dụng AWS RDS để phát triển Product features
- Branch: `feature/product-module`

### **Người 3: Order Module**
- Sử dụng AWS RDS để phát triển Order features
- Branch: `feature/order-module`

### **Người 4: Payment Module**
- Sử dụng AWS RDS để phát triển Payment features
- Branch: `feature/payment-module`

### **Workflow hàng ngày:**

```bash
# 1. Pull code mới nhất
git checkout main
git pull origin main

# 2. Checkout branch của mình
git checkout feature/your-module

# 3. Chạy với AWS profile
mvn spring-boot:run -Dspring-boot.run.profiles=aws

# 4. Code và test
# Database được share nên mọi người thấy data của nhau

# 5. Commit và push
git add .
git commit -m "feat: your feature"
git push origin feature/your-module
```

---

## 🎭 DEVELOPMENT VS PRODUCTION

### **Môi trường Development (Hiện tại):**
```
Profile: aws
Database: AWS RDS (shared)
URL: http://localhost:5055/UTE_Fashion
```

### **Môi trường Local (Optional):**
```
Profile: local
Database: SQL Server trên máy bạn
URL: http://localhost:5055/UTE_Fashion
```

**Khi nào dùng Local?**
- Khi muốn test không ảnh hưởng database chung
- Khi không có internet
- Khi muốn test với data riêng

---

## 🐛 XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi 1: Connection timeout**
```
Error: Connection is not available, request timed out after 30000ms
```

**Giải pháp:**
- Kiểm tra internet
- Kiểm tra endpoint có đúng không
- Liên hệ Team Leader kiểm tra AWS RDS đang chạy

### **Lỗi 2: Login failed**
```
Error: Login failed for user 'admin'
```

**Giải pháp:**
- Kiểm tra username/password
- Copy lại password từ Team Leader (có thể copy nhầm)
- Kiểm tra không có khoảng trắng thừa

### **Lỗi 3: Database không tồn tại**
```
Error: Cannot open database "UTE_Fashion"
```

**Giải pháp:**
- Liên hệ Team Leader
- Database có thể chưa được tạo trên AWS RDS

### **Lỗi 4: Profile không hoạt động**
```
Using default profile...
```

**Giải pháp:**
- Kiểm tra lại VM arguments: `-Dspring.profiles.active=aws`
- Hoặc dùng: `mvn spring-boot:run -Dspring-boot.run.profiles=aws`

---

## 📊 QUẢN LÝ DATA CHUNG

### **Quy tắc khi làm việc với database chung:**

**✅ ĐƯỢC PHÉP:**
- INSERT data test của module mình
- SELECT để xem data
- UPDATE/DELETE data test của mình
- Tạo stored procedures/functions cho module mình

**❌ KHÔNG ĐƯỢC:**
- DELETE tất cả data trong bảng
- DROP tables
- ALTER schema không báo team
- INSERT data rác, không có ý nghĩa
- Thay đổi data của người khác

### **Best Practices:**

**1. Prefix test data:**
```sql
-- VD: Người 2 tạo products
INSERT INTO Products (product_name, ...) VALUES 
('TEST_P2_Áo thun', ...),
('TEST_P2_Quần jean', ...);
```

**2. Comment trong code:**
```java
// TODO: Remove test data before merge to main
User testUser = new User("test_user_person3", ...);
```

**3. Cleanup trước khi demo:**
```sql
-- Xóa test data của mình
DELETE FROM Products WHERE product_name LIKE 'TEST_P2_%';
```

---

## 📝 SAMPLE DATA

Database đã có sẵn sample data:

**Users:**
- Username: `admin` / Password: `123456`
- Username: `user1` / Password: `123456`

**Products:**
- 17 sản phẩm mẫu (áo, quần, giày...)

**Orders:**
- 3 đơn hàng mẫu

→ Bạn có thể dùng để test!

---

## 🔄 CẬP NHẬT DATABASE SCHEMA

Khi cần thêm tables hoặc columns mới:

**Option 1: Hibernate tự động (Khuyến nghị)**
```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update
```
→ Chỉ cần tạo Entity mới, Hibernate tự tạo table

**Option 2: Manual SQL Script**
1. Viết migration script
2. Share với team qua Git
3. Team Leader chạy trên AWS RDS
4. Thông báo cho team

---

## 📞 LIÊN HỆ HỖ TRỢ

**Khi gặp vấn đề:**

1. **Kiểm tra lại các bước setup** ⬆️
2. **Hỏi trong group Zalo/Discord**
3. **Liên hệ Team Leader (Người 1)**
4. **Tạo Issue trên GitHub** (nếu là bug)

**Thông tin cần cung cấp khi hỏi:**
- Screenshot lỗi
- File `application-aws.properties` (BỎ password)
- Logs từ console
- Bước đã làm

---

## ✅ CHECKLIST HOÀN THÀNH SETUP

- [ ] Clone project từ Git
- [ ] Copy file `application-aws.properties.example` thành `application-aws.properties`
- [ ] Lấy credentials từ Team Leader
- [ ] Điền thông tin vào `application-aws.properties`
- [ ] Add file vào `.gitignore` (đã có sẵn)
- [ ] Chạy với profile `aws`
- [ ] Thấy log "UTE Fashion Application Started!"
- [ ] Truy cập `http://localhost:5055/UTE_Fashion`
- [ ] Đăng nhập với `admin/123456`
- [ ] Test các chức năng cơ bản

---

## 🎉 XEM THÀNH VIÊN KHÁC ĐANG LÀM GÌ

Vì dùng chung database, bạn có thể:

```sql
-- Xem ai vừa đăng nhập
SELECT username, last_login 
FROM Users 
WHERE last_login IS NOT NULL
ORDER BY last_login DESC;

-- Xem products mới được tạo (bởi Người 2)
SELECT product_name, created_at 
FROM Products 
ORDER BY created_at DESC;

-- Xem orders mới (bởi Người 3)
SELECT order_number, order_date, total_amount
FROM Orders
ORDER BY order_date DESC;
```

→ Real-time collaboration! 🚀

---

**Chúc team làm việc hiệu quả với AWS RDS! ☁️**

**Mọi thắc mắc, hỏi Team Leader hoặc group chat!** 💬

