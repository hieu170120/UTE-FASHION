# HƯỚNG DẪN SETUP EMAIL CHO TÍNH NĂNG QUÊN MẬT KHẨU

## Tổng Quan

Tính năng quên mật khẩu đã được hoàn thành với các thành phần sau:

### ✅ Các Tính Năng Đã Hoàn Thành

1. **Entity PasswordResetToken** - Lưu trữ thông tin OTP và token reset
2. **Repository PasswordResetTokenRepository** - Truy vấn database cho password reset
3. **Service ForgotPasswordService** - Logic xử lý gửi OTP và reset password
4. **Controller ForgotPasswordController** - API endpoints cho forgot password
5. **Frontend Pages** - Trang forgot-password.html và reset-password.html
6. **Security Configuration** - Cập nhật SecurityConfig cho các endpoint mới

### 🔧 Cấu Hình Email

Để sử dụng tính năng quên mật khẩu, bạn cần cấu hình email trong `application.properties`:

```properties
# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email Template Configuration
app.email.from=noreply@utefashion.com
app.email.from.name=UTE Fashion
```

### 📧 Hướng Dẫn Setup Gmail

#### Bước 1: Tạo App Password cho Gmail

1. Đăng nhập vào Gmail
2. Vào **Google Account Settings** → **Security**
3. Bật **2-Step Verification** nếu chưa bật
4. Tạo **App Password**:
   - Chọn **App passwords**
   - Chọn **Mail** và **Other (Custom name)**
   - Nhập tên: "UTE Fashion App"
   - Copy password được tạo (16 ký tự)

#### Bước 2: Cập nhật application.properties

```properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-16-character-app-password
```

### 🚀 Các Endpoint API

#### 1. Gửi OTP Quên Mật Khẩu
```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 2. Reset Mật Khẩu với OTP
```
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "user@example.com",
  "otpCode": "123456",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

#### 3. Gửi Lại OTP
```
POST /api/auth/resend-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### 🌐 Trang Web

#### 1. Trang Quên Mật Khẩu
- **URL**: `/forgot-password`
- **Chức năng**: Nhập email để nhận OTP

#### 2. Trang Reset Mật Khẩu
- **URL**: `/reset-password`
- **Chức năng**: Nhập OTP và mật khẩu mới

### 🔒 Bảo Mật

1. **OTP Expiration**: OTP có hiệu lực trong 15 phút
2. **Rate Limiting**: Tối đa 3 lần gửi OTP trong 1 giờ
3. **Token Cleanup**: Tự động xóa các token đã hết hạn
4. **Password Hashing**: Sử dụng BCrypt để mã hóa mật khẩu

### 📱 Tính Năng Frontend

1. **Auto-focus**: Tự động focus vào input đầu tiên
2. **OTP Format**: Chỉ cho phép nhập số, tự động format
3. **Password Toggle**: Nút hiện/ẩn mật khẩu
4. **Resend OTP**: Nút gửi lại OTP với confirmation
5. **Real-time Validation**: Validation ngay khi nhập

### 🧪 Test Tính Năng

1. **Truy cập**: `http://localhost:5055/UTE_Fashion/forgot-password`
2. **Nhập email** của user có trong database
3. **Kiểm tra email** để nhận OTP
4. **Nhập OTP** và mật khẩu mới
5. **Đăng nhập** với mật khẩu mới

### ⚠️ Lưu Ý Quan Trọng

1. **Email Configuration**: Phải cấu hình đúng email SMTP
2. **App Password**: Sử dụng App Password, không phải password thường
3. **Database**: Đảm bảo database có bảng `PasswordResetTokens`
4. **Network**: Đảm bảo server có thể kết nối SMTP

### 🔧 Troubleshooting

#### Lỗi "Authentication failed"
- Kiểm tra username/password email
- Đảm bảo đã bật 2-Step Verification
- Sử dụng App Password thay vì password thường

#### Lỗi "Connection timeout"
- Kiểm tra firewall
- Thử port 465 với SSL thay vì 587 với TLS

#### Không nhận được email
- Kiểm tra Spam folder
- Kiểm tra email address có đúng không
- Kiểm tra logs của application

### 📊 Database Schema

Bảng `PasswordResetTokens` sẽ được tạo tự động với các trường:
- `token_id` (Primary Key)
- `token` (Unique token string)
- `otp_code` (6-digit OTP)
- `email` (User email)
- `expires_at` (Expiration time)
- `is_used` (Used flag)
- `created_at` (Creation time)
- `used_at` (Usage time)
- `user_id` (Foreign key to Users table)

---

**Tính năng quên mật khẩu đã sẵn sàng sử dụng!** 🎉

