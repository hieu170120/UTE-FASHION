# Hệ Thống Authentication với JWT - UTE Fashion

## Tổng Quan

Dự án UTE Fashion đã được tích hợp hệ thống authentication hoàn chỉnh với JWT (JSON Web Token), hỗ trợ cả web form và REST API.

## Các Tính Năng Đã Hoàn Thành

### ✅ 1. Đăng Ký (Register)
- Validation đầy đủ (username, email, password, confirm password)
- Kiểm tra username và email đã tồn tại
- Mã hóa password bằng BCrypt
- Hỗ trợ cả web form và REST API

### ✅ 2. Đăng Nhập (Login)
- Xác thực username và password
- Tạo JWT token với thời hạn 24 giờ
- Lưu thông tin user vào session (cho web form)
- Trả về JWT token (cho REST API)
- Cập nhật last login time

### ✅ 3. Đăng Xuất (Logout)
- Xóa session (cho web form)
- Hướng dẫn xóa token ở client (cho REST API)

### ✅ 4. JWT Token Management
- Tạo JWT token với HS256 algorithm
- Validate token
- Trích xuất thông tin từ token
- Auto-authentication từ token trong request header

### ✅ 5. Security Configuration
- Stateless session management
- JWT authentication filter
- Public và protected endpoints
- Role-based access control (ADMIN role)

## Cấu Trúc Code

```
src/main/java/com/example/demo/
├── config/
│   └── SecurityConfig.java              # Cấu hình Spring Security với JWT
├── controller/
│   └── AuthController.java              # Controller xử lý authentication
├── dto/
│   ├── LoginRequest.java                # DTO cho login request
│   ├── RegisterRequest.java             # DTO cho register request
│   └── AuthResponse.java                # DTO cho authentication response (với token)
├── entity/
│   └── User.java                        # Entity User
├── repository/
│   └── UserRepository.java              # Repository cho User
├── security/
│   ├── JwtUtil.java                     # Utility class cho JWT operations
│   ├── JwtAuthenticationFilter.java     # Filter xác thực JWT token
│   └── CustomUserDetailsService.java    # Load user cho Spring Security
└── service/
    └── AuthService.java                 # Service xử lý business logic authentication
```

## Endpoints

### Web Form Endpoints
- `GET /login` - Hiển thị trang đăng nhập
- `POST /login` - Xử lý đăng nhập (sử dụng session)
- `GET /register` - Hiển thị trang đăng ký
- `POST /register` - Xử lý đăng ký
- `GET /logout` - Đăng xuất (xóa session)

### REST API Endpoints
- `POST /api/auth/register` - Đăng ký (trả về JSON)
- `POST /api/auth/login` - Đăng nhập (trả về JWT token)
- `POST /api/auth/logout` - Đăng xuất (stateless)

## Cấu Hình

### application.properties
```properties
# JWT Configuration
jwt.secret=UTE_Fashion_JWT_Secret_Key_2024_This_Should_Be_At_Least_256_Bits_Long_For_HS256_Algorithm
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Dependencies (pom.xml)
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Cách Sử Dụng

### 1. Đăng Ký Tài Khoản Mới

**Web Form:**
- Truy cập: `http://localhost:5055/UTE_Fashion/register`
- Điền thông tin và submit form

**REST API:**
```bash
curl -X POST http://localhost:5055/UTE_Fashion/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "John Doe",
    "phoneNumber": "0123456789"
  }'
```

### 2. Đăng Nhập

**Web Form:**
- Truy cập: `http://localhost:5055/UTE_Fashion/login`
- Nhập username và password

**REST API:**
```bash
curl -X POST http://localhost:5055/UTE_Fashion/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

Response sẽ trả về JWT token:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

### 3. Sử Dụng Token Để Truy Cập API

```bash
curl -X GET http://localhost:5055/UTE_Fashion/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Flow Hoạt Động

### Authentication Flow

```
1. User gửi login request
   ↓
2. AuthController nhận request
   ↓
3. AuthService xác thực username/password
   ↓
4. AuthenticationManager verify credentials
   ↓
5. JwtUtil tạo JWT token
   ↓
6. Trả về AuthResponse với token
   ↓
7. Client lưu token và gửi kèm trong các request tiếp theo
```

### Authorization Flow

```
1. Client gửi request với token trong header
   ↓
2. JwtAuthenticationFilter intercept request
   ↓
3. Extract token từ Authorization header
   ↓
4. JwtUtil validate token
   ↓
5. CustomUserDetailsService load user từ DB
   ↓
6. Set authentication vào SecurityContext
   ↓
7. Request được xử lý bởi controller
```

## Security Features

### 1. Password Security
- Mã hóa password bằng BCrypt
- Không lưu plain text password
- Validation password strength

### 2. Token Security
- JWT signed với secret key
- Token expiration (24 hours)
- Token validation trên mỗi request
- Stateless authentication

### 3. Endpoint Protection
- Public endpoints cho login/register
- Protected endpoints yêu cầu authentication
- Role-based access control

### 4. Session Management
- Stateless cho REST API (JWT)
- Session-based cho web form
- Hỗ trợ cả hai phương thức đồng thời

## Testing

### 1. Test Đăng Ký
```bash
# Test đăng ký thành công
POST /api/auth/register
{
  "username": "testuser1",
  "email": "test1@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "fullName": "Test User 1"
}

# Test username đã tồn tại
POST /api/auth/register
{
  "username": "testuser1",  # Username đã tồn tại
  "email": "test2@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "fullName": "Test User 2"
}
```

### 2. Test Đăng Nhập
```bash
# Test login thành công
POST /api/auth/login
{
  "username": "testuser1",
  "password": "password123"
}

# Test login sai password
POST /api/auth/login
{
  "username": "testuser1",
  "password": "wrongpassword"
}
```

### 3. Test Protected Endpoint
```bash
# Không có token - 401 Unauthorized
GET /api/products

# Có token hợp lệ - 200 OK
GET /api/products
Authorization: Bearer <valid-token>

# Token hết hạn - 401 Unauthorized
GET /api/products
Authorization: Bearer <expired-token>
```

## Troubleshooting

### Lỗi Thường Gặp

#### 1. "Cannot find symbol" errors khi compile
- **Nguyên nhân**: Lombok chưa được cài đặt hoặc enable trong IDE
- **Giải pháp**: 
  - Cài đặt Lombok plugin trong IDE
  - Enable annotation processing trong IDE settings
  - Clean và rebuild project

#### 2. Token không hoạt động
- **Nguyên nhân**: Token không được gửi đúng format
- **Giải pháp**: Đảm bảo header có format: `Authorization: Bearer <token>`

#### 3. 401 Unauthorized khi truy cập API
- **Nguyên nhân**: Token không hợp lệ hoặc hết hạn
- **Giải pháp**: 
  - Kiểm tra token còn hạn sử dụng
  - Login lại để lấy token mới
  - Kiểm tra secret key trong application.properties

#### 4. CORS errors khi gọi API từ frontend
- **Nguyên nhân**: CORS chưa được cấu hình
- **Giải pháp**: Thêm CORS configuration trong SecurityConfig

## Tài Liệu Tham Khảo

- [API Authentication Guide](./API_AUTHENTICATION_GUIDE.md) - Hướng dẫn chi tiết về API
- [JWT.io](https://jwt.io/) - JWT documentation
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)

## Tính Năng Sẽ Phát Triển Tiếp

### Phase 2
- [ ] Refresh Token mechanism
- [ ] Token blacklist cho logout
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Remember me functionality

### Phase 3
- [ ] Two-factor authentication (2FA)
- [ ] OAuth2 social login (Google, Facebook)
- [ ] Rate limiting cho login attempts
- [ ] Account lockout after failed attempts
- [ ] Password history và complexity rules

### Phase 4
- [ ] Audit logging cho security events
- [ ] IP whitelist/blacklist
- [ ] Device management
- [ ] Session management dashboard
- [ ] Security analytics và monitoring

## Đóng Góp

Nếu bạn muốn đóng góp vào dự án, vui lòng:
1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

UTE Fashion - Internal Project



