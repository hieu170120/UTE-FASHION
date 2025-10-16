# HƯỚNG DẪN SỬ DỤNG AWS RDS SQL SERVER

## 🌐 TỔNG QUAN

**AWS RDS (Relational Database Service)** cho phép bạn chạy SQL Server trên cloud, có nhiều lợi ích:
- ✅ Truy cập từ mọi nơi
- ✅ Backup tự động
- ✅ Bảo mật cao
- ✅ Dễ scale
- ✅ Team có thể cùng dùng 1 database

---

## 📋 BƯỚC 1: TẠO AWS RDS SQL SERVER

### **1.1. Đăng ký AWS Account (nếu chưa có)**
- Truy cập: https://aws.amazon.com/
- Click "Create an AWS Account"
- Điền thông tin (cần thẻ tín dụng, nhưng có Free Tier)

### **1.2. Tạo RDS Instance**

**Bước 1: Vào AWS Console**
```
https://console.aws.amazon.com/rds/
```

**Bước 2: Create Database**
- Click "Create database"
- Chọn "Standard create"

**Bước 3: Engine Options**
- Engine type: **Microsoft SQL Server**
- Edition: **SQL Server Express Edition** (Free Tier)
- Version: **SQL Server 2019** (hoặc mới nhất)

**Bước 4: Templates**
- Chọn: **Free tier** (nếu đủ điều kiện)
- Hoặc: **Dev/Test** (cho dự án học)

**Bước 5: Settings**
```
DB instance identifier: ute-fashion-db
Master username: admin
Master password: YourStrongPassword123!
Confirm password: YourStrongPassword123!
```

**Bước 6: Instance Configuration**
```
DB instance class: db.t3.micro (Free Tier)
Storage type: General Purpose (SSD)
Allocated storage: 20 GB
```

**Bước 7: Connectivity**
```
Virtual Private Cloud (VPC): Default VPC
Subnet group: default
Public access: YES ✅ (quan trọng!)
VPC security group: Create new
  - Name: ute-fashion-sg
```

**Bước 8: Additional Configuration**
```
Initial database name: UTE_Fashion
Backup retention: 7 days
Enable encryption: YES (khuyến nghị)
```

**Bước 9: Click "Create database"**
- Đợi 5-10 phút để AWS tạo database

---

## 🔐 BƯỚC 2: CẤU HÌNH SECURITY GROUP

### **2.1. Mở Port 1433 (SQL Server)**

**Bước 1: Vào EC2 Console**
```
https://console.aws.amazon.com/ec2/
```

**Bước 2: Security Groups**
- Sidebar → Network & Security → Security Groups
- Tìm security group: `ute-fashion-sg`
- Click vào security group đó

**Bước 3: Edit Inbound Rules**
- Tab "Inbound rules" → Click "Edit inbound rules"
- Click "Add rule"

**Bước 4: Thêm Rule**
```
Type: MSSQL
Protocol: TCP
Port range: 1433
Source: My IP (hoặc 0.0.0.0/0 cho phép tất cả)
Description: Allow SQL Server access
```

**Bước 5: Save rules**

⚠️ **Lưu ý:** `0.0.0.0/0` cho phép truy cập từ mọi nơi (không an toàn cho production)

---

## 📝 BƯỚC 3: LẤY THÔNG TIN KẾT NỐI

### **3.1. Lấy Endpoint**

**Bước 1: Vào RDS Console**
```
https://console.aws.amazon.com/rds/
```

**Bước 2: Chọn Database**
- Click vào database: `ute-fashion-db`
- Tab "Connectivity & security"

**Bước 3: Copy Endpoint**
```
Endpoint: ute-fashion-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com
Port: 1433
```

**Ví dụ đầy đủ:**
```
Endpoint: ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com
Port: 1433
Master username: admin
Master password: YourStrongPassword123!
Database name: UTE_Fashion
```

---

## ⚙️ BƯỚC 4: CẬP NHẬT APPLICATION.PROPERTIES

### **4.1. Cấu hình mới cho AWS RDS**

Mở file: `src/main/resources/application.properties`

**Thay đổi từ:**
```properties
# Local SQL Server
spring.datasource.url=jdbc:sqlserver://DESKTOP-HPRPTG8:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123456
```

**Sang:**
```properties
# AWS RDS SQL Server
spring.datasource.url=jdbc:sqlserver://ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
spring.datasource.username=admin
spring.datasource.password=YourStrongPassword123!
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
```

### **4.2. Hoặc dùng Environment Variables (Khuyến nghị)**

**application.properties:**
```properties
# AWS RDS SQL Server - Using Environment Variables
spring.datasource.url=${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=UTE_Fashion}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:123456}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**Trong Eclipse/IntelliJ, set Environment Variables:**
```
DB_URL=jdbc:sqlserver://ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
DB_USERNAME=admin
DB_PASSWORD=YourStrongPassword123!
```

---

## 🧪 BƯỚC 5: TEST KẾT NỐI

### **5.1. Test bằng SQL Server Management Studio (SSMS)**

**Bước 1: Mở SSMS**

**Bước 2: Connect to Server**
```
Server type: Database Engine
Server name: ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com,1433
Authentication: SQL Server Authentication
Login: admin
Password: YourStrongPassword123!
```

**Bước 3: Click Connect**

Nếu kết nối thành công → Bạn sẽ thấy database `UTE_Fashion`

### **5.2. Test bằng Spring Boot**

**Bước 1: Chạy application**
```bash
mvn spring-boot:run
```

**Bước 2: Xem logs**
```
Hibernate: create table Users (...)
...
UTE Fashion Application Started!
```

**Bước 3: Truy cập**
```
http://localhost:5055/UTE_Fashion/
```

---

## 📊 BƯỚC 6: TẠO DATABASE SCHEMA

### **6.1. Chạy SQL Scripts**

**Option 1: Dùng SSMS**
- Connect vào AWS RDS
- Mở file: `database/UTE_Fashion_Database_Schema.sql`
- Sửa dòng đầu:
```sql
-- BỎ dòng này (vì database đã tồn tại):
-- CREATE DATABASE UTE_Fashion;

-- Chỉ giữ:
USE UTE_Fashion;
GO

-- Và các CREATE TABLE...
```
- Execute (F5)

**Option 2: Để Hibernate tự tạo**
```properties
# application.properties
spring.jpa.hibernate.ddl-auto=create
# Lần đầu chạy: create
# Sau đó đổi thành: update
```

### **6.2. Insert Sample Data**

**Chạy file:** `database/UTE_Fashion_Sample_Data.sql`
- Connect vào AWS RDS bằng SSMS
- Execute script

---

## 👥 BƯỚC 7: CHIA SẺ CHO TEAM

### **7.1. Tạo file cấu hình riêng**

**Tạo file:** `application-aws.properties`
```properties
# AWS RDS Configuration
spring.datasource.url=jdbc:sqlserver://ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
spring.datasource.username=admin
spring.datasource.password=YourStrongPassword123!
```

**Chạy với profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

### **7.2. Chia sẻ thông tin cho team**

**Tạo file:** `AWS_DATABASE_INFO.txt` (KHÔNG commit lên Git!)
```
AWS RDS Endpoint: ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com
Port: 1433
Username: admin
Password: YourStrongPassword123!
Database: UTE_Fashion

Connection String:
jdbc:sqlserver://ute-fashion-db.c9x8y7z6w5v4.us-east-1.rds.amazonaws.com:1433;databaseName=UTE_Fashion;encrypt=true;trustServerCertificate=true
```

**Thêm vào .gitignore:**
```
AWS_DATABASE_INFO.txt
application-aws.properties
```

### **7.3. Team members connect**

Mỗi người trong team chỉ cần:
1. Copy thông tin connection
2. Cập nhật `application.properties`
3. Chạy application

---

## 💰 CHI PHÍ AWS RDS

### **Free Tier (12 tháng đầu):**
```
✅ db.t3.micro instance
✅ 20 GB storage
✅ 20 GB backup storage
✅ Đủ cho dự án học tập
```

### **Sau Free Tier:**
```
💵 db.t3.micro: ~$15-20/tháng
💵 Storage: ~$0.115/GB/tháng
💵 Backup: ~$0.095/GB/tháng

Tổng ước tính: ~$20-30/tháng
```

### **Tiết kiệm chi phí:**
- Stop instance khi không dùng (tối đa 7 ngày)
- Xóa snapshot backup không cần thiết
- Dùng Reserved Instances (nếu dùng lâu dài)

---

## 🔒 BẢO MẬT

### **Best Practices:**

**1. Không hardcode password**
```properties
# ❌ BAD
spring.datasource.password=YourStrongPassword123!

# ✅ GOOD
spring.datasource.password=${DB_PASSWORD}
```

**2. Sử dụng Secrets Manager**
- AWS Secrets Manager
- Spring Cloud AWS
- Lưu credentials an toàn

**3. Giới hạn IP access**
```
Security Group → Inbound Rules
Source: Chỉ IP của team (không dùng 0.0.0.0/0)
```

**4. Enable SSL/TLS**
```properties
spring.datasource.url=...;encrypt=true;trustServerCertificate=false
```

**5. Backup thường xuyên**
- Automated backups: 7 days
- Manual snapshots trước khi deploy

---

## 🚨 XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi 1: Connection timeout**
```
Error: Connection timed out
```

**Giải pháp:**
- Kiểm tra Security Group đã mở port 1433
- Kiểm tra Public access = YES
- Kiểm tra VPC/Subnet settings

### **Lỗi 2: Login failed**
```
Error: Login failed for user 'admin'
```

**Giải pháp:**
- Kiểm tra username/password
- Reset password trên AWS Console
- Kiểm tra database name đúng chưa

### **Lỗi 3: SSL Certificate error**
```
Error: SSL Security error
```

**Giải pháp:**
```properties
# Thêm vào connection string:
;trustServerCertificate=true
```

### **Lỗi 4: Database không tồn tại**
```
Error: Cannot open database "UTE_Fashion"
```

**Giải pháp:**
- Tạo database trong RDS Console
- Hoặc chạy: `CREATE DATABASE UTE_Fashion;`

---

## 📋 CHECKLIST HOÀN THÀNH

- [ ] Tạo AWS RDS instance
- [ ] Cấu hình Security Group (port 1433)
- [ ] Lấy endpoint và credentials
- [ ] Cập nhật application.properties
- [ ] Test connection bằng SSMS
- [ ] Chạy database schema script
- [ ] Insert sample data
- [ ] Test Spring Boot application
- [ ] Chia sẻ thông tin cho team
- [ ] Thêm credentials vào .gitignore

---

## 🎯 SO SÁNH LOCAL vs AWS RDS

| Tiêu chí | Local SQL Server | AWS RDS |
|----------|------------------|---------|
| **Truy cập** | Chỉ máy local | Mọi nơi có internet |
| **Team collaboration** | Khó | Dễ dàng |
| **Backup** | Manual | Tự động |
| **Bảo mật** | Phụ thuộc máy | AWS security |
| **Chi phí** | Free | ~$20/tháng |
| **Setup** | Dễ | Cần học AWS |
| **Performance** | Phụ thuộc máy | Ổn định |

---

## 📚 TÀI LIỆU THAM KHẢO

- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [AWS RDS SQL Server](https://aws.amazon.com/rds/sqlserver/)
- [Spring Boot with AWS RDS](https://spring.io/guides/gs/accessing-data-mysql/)

---

## 💡 TIPS

**Tip 1: Dùng 2 profiles**
```properties
# application.properties (local)
spring.datasource.url=jdbc:sqlserver://localhost:1433...

# application-aws.properties (AWS)
spring.datasource.url=jdbc:sqlserver://aws-endpoint...
```

**Tip 2: Connection pooling**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

**Tip 3: Monitor costs**
- Vào AWS Billing Dashboard
- Set up Budget alerts
- Monitor usage hàng ngày

---

**Chúc bạn setup AWS RDS thành công! 🚀☁️**

**Có thắc mắc? Liên hệ AWS Support hoặc team leader!**

