# Hướng Dẫn Sử Dụng API Authentication với JWT

## Tổng Quan

Hệ thống authentication đã được xây dựng với JWT (JSON Web Token) để xác thực người dùng. Hệ thống hỗ trợ cả hai phương thức:
1. **Web Form Authentication** - Sử dụng session cho giao diện web
2. **REST API Authentication** - Sử dụng JWT token cho mobile app hoặc SPA

## Cấu Trúc Hệ Thống

### 1. JWT Configuration
- **Secret Key**: Được cấu hình trong `application.properties`
- **Token Expiration**: 24 giờ (86400000 milliseconds)
- **Algorithm**: HS256

### 2. Các Component Chính

#### JwtUtil (`src/main/java/com/example/demo/security/JwtUtil.java`)
- Tạo JWT token từ username
- Validate JWT token
- Trích xuất thông tin từ token

#### JwtAuthenticationFilter (`src/main/java/com/example/demo/security/JwtAuthenticationFilter.java`)
- Filter tự động xác thực JWT token từ request header
- Lấy token từ header `Authorization: Bearer <token>`
- Set authentication vào SecurityContext

#### CustomUserDetailsService (`src/main/java/com/example/demo/security/CustomUserDetailsService.java`)
- Load user từ database cho Spring Security
- Kiểm tra account active status

#### SecurityConfig (`src/main/java/com/example/demo/config/SecurityConfig.java`)
- Cấu hình Spring Security với JWT
- Stateless session management
- Public và protected endpoints

## API Endpoints

### 1. Đăng Ký (Register)

#### Web Form
```
POST /register
Content-Type: application/x-www-form-urlencoded

username=johndoe
email=john@example.com
password=password123
confirmPassword=password123
fullName=John Doe
phoneNumber=0123456789
```

#### REST API
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0123456789"
}
```

**Response Success:**
```json
{
  "message": "Đăng ký thành công!",
  "userId": 1,
  "username": "johndoe"
}
```

**Response Error:**
```json
{
  "error": "Tên đăng nhập đã tồn tại"
}
```

### 2. Đăng Nhập (Login)

#### Web Form
```
POST /login
Content-Type: application/x-www-form-urlencoded

username=johndoe
password=password123
```

#### REST API
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response Success:**
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

**Response Error:**
```json
{
  "error": "Tên đăng nhập hoặc mật khẩu không đúng"
}
```

### 3. Đăng Xuất (Logout)

#### Web Form
```
GET /logout
```

#### REST API
```
POST /api/auth/logout
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "message": "Đăng xuất thành công! Vui lòng xóa token ở phía client."
}
```

**Lưu ý:** Với JWT stateless, client cần tự xóa token ở phía mình (localStorage, sessionStorage, etc.)

## Cách Sử Dụng JWT Token

### 1. Lưu Token Sau Khi Login

**JavaScript Example:**
```javascript
// Sau khi login thành công
fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'johndoe',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => {
  // Lưu token vào localStorage
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify({
    userId: data.userId,
    username: data.username,
    email: data.email,
    fullName: data.fullName
  }));
});
```

### 2. Gửi Token Trong Request

**JavaScript Example:**
```javascript
// Lấy token từ localStorage
const token = localStorage.getItem('token');

// Gửi request với token
fetch('/api/products', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log(data);
});
```

### 3. Xóa Token Khi Logout

**JavaScript Example:**
```javascript
function logout() {
  // Xóa token và user info
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  
  // Redirect về trang login
  window.location.href = '/login';
}
```

## Security Configuration

### Public Endpoints (Không cần authentication)
- `/` - Trang chủ
- `/login` - Trang đăng nhập
- `/register` - Trang đăng ký
- `/logout` - Đăng xuất
- `/api/auth/**` - API authentication endpoints
- `/static/**`, `/css/**`, `/js/**`, `/images/**` - Static resources

### Protected Endpoints (Cần authentication)
- `/api/**` - Tất cả API endpoints (trừ `/api/auth/**`)
- Các trang khác

### Admin Endpoints (Cần role ADMIN)
- `/admin/**` - Tất cả admin endpoints

## Error Handling

### Common Error Responses

**401 Unauthorized - Token không hợp lệ hoặc hết hạn:**
```json
{
  "error": "Unauthorized",
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

**403 Forbidden - Không có quyền truy cập:**
```json
{
  "error": "Forbidden",
  "message": "Bạn không có quyền truy cập tài nguyên này"
}
```

**400 Bad Request - Dữ liệu không hợp lệ:**
```json
{
  "error": "Tên đăng nhập không được để trống"
}
```

## Testing với Postman/cURL

### 1. Register
```bash
curl -X POST http://localhost:5055/UTE_Fashion/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Test User",
    "phoneNumber": "0123456789"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:5055/UTE_Fashion/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Sử dụng Token để truy cập API
```bash
curl -X GET http://localhost:5055/UTE_Fashion/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Best Practices

### 1. Token Security
- Không lưu token trong cookie nếu không set httpOnly
- Sử dụng HTTPS trong production
- Set token expiration time hợp lý
- Implement refresh token mechanism cho production

### 2. Password Security
- Mật khẩu được hash bằng BCrypt
- Minimum password length: 6 characters
- Nên yêu cầu password phức tạp hơn trong production

### 3. Error Messages
- Không tiết lộ thông tin nhạy cảm trong error messages
- Sử dụng generic error messages cho authentication failures

## Troubleshooting

### Token không hoạt động
1. Kiểm tra token có được gửi đúng format: `Authorization: Bearer <token>`
2. Kiểm tra token chưa hết hạn
3. Kiểm tra secret key trong `application.properties`

### CORS Issues
- Nếu gọi API từ domain khác, cần cấu hình CORS
- Thêm `@CrossOrigin` annotation hoặc cấu hình global CORS

### Session vs JWT
- Web form sử dụng session (stateful)
- REST API sử dụng JWT (stateless)
- Có thể sử dụng cả hai cùng lúc

## Next Steps

### Improvements cho Production
1. **Refresh Token**: Implement refresh token mechanism
2. **Token Blacklist**: Implement token blacklist cho logout
3. **Rate Limiting**: Thêm rate limiting cho login endpoint
4. **Email Verification**: Xác thực email sau khi đăng ký
5. **Password Reset**: Chức năng quên mật khẩu
6. **2FA**: Two-factor authentication
7. **OAuth2**: Social login (Google, Facebook, etc.)

## Liên Hệ & Hỗ Trợ

Nếu có vấn đề hoặc câu hỏi, vui lòng liên hệ team development.

