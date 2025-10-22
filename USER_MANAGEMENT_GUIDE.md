# 📋 CHỨC NĂNG QUẢN LÝ USER - UTE FASHION ADMIN

## 🎯 **Tổng quan**
Chức năng quản lý User cho phép admin:
- Xem danh sách tất cả người dùng với tìm kiếm và phân trang
- Khóa/mở khóa tài khoản người dùng
- Xem lịch sử đặt hàng của từng user
- Thống kê tổng quan về người dùng

## 🚀 **Cách truy cập**
1. Đăng nhập với tài khoản admin
2. Truy cập: `http://localhost:8080/admin/users`
3. Hoặc từ Dashboard admin → Quản lý Người dùng

## 🔧 **Các tính năng chính**

### 1. **Danh sách User**
- **Tìm kiếm**: Theo username, email, hoặc họ tên
- **Lọc**: Theo trạng thái (Tất cả/Đang hoạt động/Bị khóa)
- **Phân trang**: 10 user/trang (có thể điều chỉnh)
- **Thông tin hiển thị**:
  - Avatar
  - Username (clickable để xem lịch sử đơn hàng)
  - Email
  - Họ tên
  - Số điện thoại
  - Trạng thái tài khoản
  - Trạng thái xác thực email
  - Lần đăng nhập cuối
  - Ngày tạo tài khoản

### 2. **Thống kê tổng quan**
- **Tổng người dùng**: Số lượng user trong hệ thống
- **Đang hoạt động**: User có thể đăng nhập
- **Bị khóa**: User bị admin khóa tài khoản
- **Mới hôm nay**: User đăng ký trong ngày

### 3. **Quản lý trạng thái tài khoản**
- **Khóa tài khoản**: Click nút 🔒 để khóa
- **Mở khóa tài khoản**: Click nút 🔓 để mở khóa
- **Xác nhận**: Có popup xác nhận trước khi thực hiện

### 4. **Lịch sử đặt hàng của User**
- **Cách xem**: Click vào username của user
- **Modal hiển thị**:
  - Thông tin user
  - Thống kê đơn hàng
  - Danh sách đơn hàng với phân trang
  - Link xem chi tiết từng đơn hàng

## 📊 **Thống kê trong Modal**

### **Thông tin User**
- Tổng đơn hàng
- Tổng chi tiêu
- Đơn hàng gần nhất

### **Thống kê theo trạng thái**
- Đang xử lý
- Đã xác nhận
- Đang giao
- Đã giao
- Đã hủy

## 🔗 **API Endpoints**

### **GET /admin/users**
- **Mục đích**: Lấy danh sách user với tìm kiếm và phân trang
- **Parameters**:
  - `page`: Số trang (default: 0)
  - `size`: Số lượng/trang (default: 10)
  - `search`: Từ khóa tìm kiếm
  - `isActive`: Lọc theo trạng thái (true/false)

### **PUT /admin/users/{id}/lock**
- **Mục đích**: Khóa tài khoản user
- **Response**: JSON với success/message

### **PUT /admin/users/{id}/unlock**
- **Mục đích**: Mở khóa tài khoản user
- **Response**: JSON với success/message

### **GET /admin/users/{id}/orders**
- **Mục đích**: Lấy lịch sử đơn hàng của user
- **Parameters**:
  - `page`: Số trang (default: 0)
  - `size`: Số lượng/trang (default: 5)
- **Response**: JSON với orders, statistics, pagination

### **GET /admin/users/{id}/orders/statistics**
- **Mục đích**: Lấy thống kê đơn hàng của user
- **Response**: JSON với statistics

## 🎨 **Giao diện**

### **Màu sắc và Icon**
- **Active**: Xanh lá (#28a745) với icon ✅
- **Inactive**: Đỏ (#dc3545) với icon 🔒
- **Verified**: Xanh dương (#17a2b8) với icon ✓
- **Unverified**: Vàng (#ffc107) với icon ⚠️

### **Responsive Design**
- Tương thích với mobile và tablet
- Bootstrap 5 framework
- Font Awesome icons

## 🔒 **Bảo mật**
- Chỉ admin mới có quyền truy cập
- Xác nhận trước khi khóa/mở khóa tài khoản
- Validation input parameters
- Transaction cho các thao tác update

## 🐛 **Xử lý lỗi**
- User không tồn tại: "Không tìm thấy user với ID: {id}"
- Tài khoản đã bị khóa: "Tài khoản đã bị khóa"
- Tài khoản đã được mở khóa: "Tài khoản đã được mở khóa"
- Lỗi hệ thống: "Có lỗi xảy ra"

## 📝 **Ghi chú**
- Tất cả thời gian hiển thị theo định dạng Việt Nam (dd/MM/yyyy HH:mm)
- Tiền tệ hiển thị theo định dạng Việt Nam (1,000,000 VNĐ)
- Modal có thể đóng bằng nút X hoặc click outside
- Phân trang tự động cập nhật khi thay đổi trang

## 🔄 **Cập nhật trong tương lai**
- Export danh sách user ra Excel
- Import user từ file CSV
- Gửi email thông báo khi khóa/mở khóa tài khoản
- Audit log chi tiết các thao tác admin
- Biểu đồ thống kê user theo thời gian

