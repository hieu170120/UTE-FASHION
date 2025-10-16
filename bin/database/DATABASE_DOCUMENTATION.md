# UTE FASHION - TÀI LIỆU CƠ SỞ DỮ LIỆU

## 📊 TỔNG QUAN

Database: **UTE_Fashion**  
DBMS: **SQL Server**  
Tổng số bảng: **30 bảng**  
Charset: **Unicode (NVARCHAR)**

---

## 📋 DANH SÁCH CÁC BẢNG

### 1. NHÓM AUTHENTICATION & USERS (5 bảng)
- `Users` - Thông tin người dùng
- `Roles` - Vai trò (Admin, User, Manager, Staff)
- `User_Roles` - Liên kết User-Role (Many-to-Many)
- `Addresses` - Địa chỉ giao hàng
- `Notifications` - Thông báo (WebSocket)

### 2. NHÓM PRODUCTS (8 bảng)
- `Categories` - Danh mục sản phẩm (Tree structure)
- `Brands` - Thương hiệu
- `Products` - Sản phẩm
- `Product_Images` - Hình ảnh sản phẩm
- `Sizes` - Kích cỡ (S, M, L, XL...)
- `Colors` - Màu sắc
- `Product_Variants` - Biến thể (Size + Color)
- `Inventory_Transactions` - Lịch sử xuất nhập kho

### 3. NHÓM SHOPPING (2 bảng)
- `Carts` - Giỏ hàng
- `Cart_Items` - Sản phẩm trong giỏ

### 4. NHÓM ORDERS (4 bảng)
- `Orders` - Đơn hàng
- `Order_Items` - Chi tiết đơn hàng
- `Order_Status_History` - Lịch sử trạng thái đơn
- `Payments` - Thanh toán

### 5. NHÓM PAYMENTS & COUPONS (3 bảng)
- `Payment_Methods` - Phương thức thanh toán
- `Coupons` - Mã giảm giá
- `Coupon_Usage` - Lịch sử sử dụng coupon

### 6. NHÓM REVIEWS (2 bảng)
- `Reviews` - Đánh giá sản phẩm
- `Review_Images` - Hình ảnh đánh giá
- `Wishlists` - Danh sách yêu thích

### 7. NHÓM CMS (4 bảng)
- `Banners` - Banner quảng cáo
- `Settings` - Cấu hình website
- `Contacts` - Liên hệ từ khách hàng

---

## 🔑 CHI TIẾT CÁC BẢNG CHÍNH

### **1. BẢNG USERS**
```sql
Users (
    user_id INT PRIMARY KEY,
    username NVARCHAR(50) UNIQUE,
    email NVARCHAR(100) UNIQUE,
    password_hash NVARCHAR(255), -- BCrypt
    full_name NVARCHAR(100),
    phone_number NVARCHAR(20),
    avatar_url NVARCHAR(500),
    date_of_birth DATE,
    gender NVARCHAR(10),
    is_active BIT,
    is_email_verified BIT,
    email_verification_token NVARCHAR(255),
    password_reset_token NVARCHAR(255),
    password_reset_expires DATETIME,
    last_login DATETIME,
    created_at DATETIME,
    updated_at DATETIME
)
```

**Chức năng:**
- Lưu thông tin người dùng
- Hỗ trợ JWT authentication
- Email verification
- Password reset
- Tracking last login

**Quan hệ:**
- 1 User → Many Addresses
- 1 User → Many Orders
- 1 User → Many Reviews
- Many-to-Many với Roles

---

### **2. BẢNG PRODUCTS**
```sql
Products (
    product_id INT PRIMARY KEY,
    product_name NVARCHAR(255),
    slug NVARCHAR(255) UNIQUE,
    sku NVARCHAR(100) UNIQUE,
    category_id INT FK → Categories,
    brand_id INT FK → Brands,
    description NVARCHAR(MAX),
    short_description NVARCHAR(500),
    price DECIMAL(18,2),
    sale_price DECIMAL(18,2),
    cost_price DECIMAL(18,2),
    stock_quantity INT,
    low_stock_threshold INT,
    weight DECIMAL(10,2),
    dimensions NVARCHAR(50),
    material NVARCHAR(255),
    is_featured BIT,
    is_new_arrival BIT,
    is_best_seller BIT,
    is_active BIT,
    view_count INT,
    sold_count INT,
    average_rating DECIMAL(3,2),
    review_count INT,
    created_at DATETIME,
    updated_at DATETIME
)
```

**Chức năng:**
- Quản lý thông tin sản phẩm
- SEO-friendly (slug)
- Quản lý tồn kho
- Tracking views, sales
- Average rating tự động

**Quan hệ:**
- Many Products → 1 Category
- Many Products → 1 Brand
- 1 Product → Many Images
- 1 Product → Many Variants
- 1 Product → Many Reviews

---

### **3. BẢNG PRODUCT_VARIANTS**
```sql
Product_Variants (
    variant_id INT PRIMARY KEY,
    product_id INT FK → Products,
    size_id INT FK → Sizes,
    color_id INT FK → Colors,
    sku NVARCHAR(100) UNIQUE,
    price_adjustment DECIMAL(18,2),
    stock_quantity INT,
    is_active BIT,
    UNIQUE (product_id, size_id, color_id)
)
```

**Chức năng:**
- Quản lý biến thể sản phẩm (Size + Color)
- Mỗi variant có giá và số lượng riêng
- Unique constraint để tránh duplicate

**Ví dụ:**
```
Product: "Áo Thun Nam Basic"
Variants:
  - Size S, Color Trắng → Stock: 20
  - Size M, Color Trắng → Stock: 30
  - Size L, Color Đen → Stock: 25
```

---

### **4. BẢNG CATEGORIES (Tree Structure)**
```sql
Categories (
    category_id INT PRIMARY KEY,
    category_name NVARCHAR(100),
    slug NVARCHAR(100) UNIQUE,
    description NVARCHAR(500),
    parent_category_id INT FK → Categories (Self-reference),
    image_url NVARCHAR(500),
    display_order INT,
    is_active BIT
)
```

**Chức năng:**
- Danh mục phân cấp (parent-child)
- Hỗ trợ unlimited levels

**Ví dụ cấu trúc:**
```
Nam (parent_id = NULL)
  ├── Áo thun nam (parent_id = 1)
  ├── Áo sơ mi nam (parent_id = 1)
  └── Quần jean nam (parent_id = 1)
Nữ (parent_id = NULL)
  ├── Áo thun nữ (parent_id = 2)
  └── Váy (parent_id = 2)
```

---

### **5. BẢNG ORDERS**
```sql
Orders (
    order_id INT PRIMARY KEY,
    order_number NVARCHAR(50) UNIQUE, -- ORD-2024-0001
    user_id INT FK → Users,
    
    -- Shipping info
    recipient_name NVARCHAR(100),
    phone_number NVARCHAR(20),
    email NVARCHAR(100),
    shipping_address NVARCHAR(500),
    ward, district, city,
    
    -- Money
    subtotal DECIMAL(18,2),
    shipping_fee DECIMAL(18,2),
    discount_amount DECIMAL(18,2),
    tax_amount DECIMAL(18,2),
    total_amount DECIMAL(18,2),
    
    -- Status
    order_status NVARCHAR(50), 
    -- 'Pending', 'Confirmed', 'Processing', 'Shipping', 'Delivered', 'Cancelled'
    payment_status NVARCHAR(50),
    -- 'Unpaid', 'Paid', 'Refunded', 'Failed'
    
    -- Timestamps
    order_date DATETIME,
    confirmed_at DATETIME,
    shipped_at DATETIME,
    delivered_at DATETIME,
    cancelled_at DATETIME
)
```

**Chức năng:**
- Quản lý đơn hàng
- Tracking trạng thái đơn hàng
- Lưu snapshot địa chỉ giao hàng
- Tính toán tổng tiền

**Order Status Flow:**
```
Pending → Confirmed → Processing → Shipping → Delivered
                                        ↓
                                   Cancelled
```

---

### **6. BẢNG CARTS**
```sql
Carts (
    cart_id INT PRIMARY KEY,
    user_id INT FK → Users (nullable),
    session_id NVARCHAR(255), -- For guest users
    created_at DATETIME,
    updated_at DATETIME
)

Cart_Items (
    cart_item_id INT PRIMARY KEY,
    cart_id INT FK → Carts,
    product_id INT FK → Products,
    variant_id INT FK → Product_Variants,
    quantity INT,
    price DECIMAL(18,2), -- Snapshot price
    created_at DATETIME
)
```

**Chức năng:**
- Hỗ trợ cả user đã login và guest
- Guest: dùng session_id
- Logged user: dùng user_id
- Lưu snapshot price để tránh thay đổi giá

---

### **7. BẢNG PAYMENTS**
```sql
Payments (
    payment_id INT PRIMARY KEY,
    order_id INT FK → Orders,
    payment_method_id INT FK → Payment_Methods,
    transaction_id NVARCHAR(255), -- From gateway
    amount DECIMAL(18,2),
    payment_status NVARCHAR(50),
    payment_gateway_response NVARCHAR(MAX), -- JSON
    paid_at DATETIME,
    refunded_at DATETIME,
    refund_amount DECIMAL(18,2)
)
```

**Chức năng:**
- Quản lý thanh toán
- Integration với VNPay, MoMo
- Lưu transaction ID từ gateway
- Hỗ trợ refund

**Payment Methods:**
- COD (Cash on Delivery)
- Bank Transfer
- VNPay
- MoMo

---

### **8. BẢNG COUPONS**
```sql
Coupons (
    coupon_id INT PRIMARY KEY,
    coupon_code NVARCHAR(50) UNIQUE,
    description NVARCHAR(500),
    discount_type NVARCHAR(20), -- 'Percentage', 'Fixed'
    discount_value DECIMAL(18,2),
    min_order_value DECIMAL(18,2),
    max_discount_amount DECIMAL(18,2),
    usage_limit INT, -- Total usage limit
    usage_count INT,
    usage_limit_per_user INT,
    valid_from DATETIME,
    valid_to DATETIME,
    is_active BIT
)
```

**Chức năng:**
- Mã giảm giá
- 2 loại: Percentage (%) hoặc Fixed (số tiền cố định)
- Giới hạn số lần sử dụng
- Giới hạn per user

**Ví dụ:**
```
WELCOME2024:
  - Type: Percentage
  - Value: 10%
  - Min order: 300,000đ
  - Max discount: 100,000đ
  - Usage limit: 100 lần
  - Per user: 1 lần
```

---

### **9. BẢNG REVIEWS**
```sql
Reviews (
    review_id INT PRIMARY KEY,
    product_id INT FK → Products,
    user_id INT FK → Users,
    order_id INT FK → Orders,
    rating INT CHECK (1-5),
    title NVARCHAR(255),
    comment NVARCHAR(MAX),
    is_verified_purchase BIT, -- Đã mua hàng
    is_approved BIT, -- Admin duyệt
    helpful_count INT,
    created_at DATETIME
)
```

**Chức năng:**
- Đánh giá sản phẩm (1-5 sao)
- Chỉ người đã mua mới được review
- Admin phê duyệt trước khi hiển thị
- Upload hình ảnh kèm theo

---

### **10. BẢNG NOTIFICATIONS (WebSocket)**
```sql
Notifications (
    notification_id INT PRIMARY KEY,
    user_id INT FK → Users (nullable),
    notification_type NVARCHAR(50),
    -- 'ORDER_STATUS', 'NEW_PRODUCT', 'PROMOTION', 'LOW_STOCK'
    title NVARCHAR(255),
    message NVARCHAR(MAX),
    link_url NVARCHAR(500),
    is_read BIT,
    created_at DATETIME
)
```

**Chức năng:**
- Thông báo real-time qua WebSocket
- user_id = NULL → Broadcast all users
- Tracking read/unread status

**Use cases:**
- Thông báo trạng thái đơn hàng
- Sản phẩm mới
- Khuyến mãi
- Admin: Đơn hàng mới, hàng sắp hết

---

## 🔗 SƠ ĐỒ QUAN HỆ (ERD)

### **Quan hệ chính:**

```
Users (1) ───< (M) User_Roles (M) >─── (1) Roles
  │
  ├──< (1:M) Addresses
  ├──< (1:M) Orders
  ├──< (1:M) Reviews
  ├──< (1:M) Wishlists
  ├──< (1:1) Carts
  └──< (1:M) Notifications

Categories (Tree: 1:M self-reference)
  │
  └──< (1:M) Products
              │
              ├──< (1:M) Product_Images
              ├──< (1:M) Product_Variants
              ├──< (1:M) Reviews
              ├──< (1:M) Cart_Items
              └──< (1:M) Order_Items

Brands (1) ───< (M) Products

Sizes (1) ───< (M) Product_Variants >─── (M) Colors

Orders (1) ───< (M) Order_Items
       (1) ───< (M) Payments
       (1) ───< (M) Order_Status_History

Coupons (1) ───< (M) Coupon_Usage
```

---

## 📈 INDEXES

### **Indexes đã tạo:**

```sql
-- Users
idx_users_email
idx_users_username
idx_users_is_active

-- Products
idx_products_category
idx_products_brand
idx_products_slug
idx_products_is_active
idx_products_created_at

-- Orders
idx_orders_user
idx_orders_order_number
idx_orders_status
idx_orders_date

-- Reviews
idx_reviews_product
idx_reviews_user

-- Notifications
idx_notifications_user
idx_notifications_is_read
```

**Lý do tạo index:**
- Tăng tốc độ query
- Các column thường xuyên WHERE, JOIN
- Unique constraints

---

## 🔒 BẢO MẬT

### **1. Password Security:**
- Password được hash bằng **BCrypt** (cost factor: 10)
- Không lưu plain text password
- Password reset qua token có thời hạn

### **2. SQL Injection Prevention:**
- Sử dụng **Parameterized Queries**
- JPA/Hibernate tự động escape

### **3. Soft Delete:**
- Không xóa hẳn data
- Sử dụng `is_active = 0` hoặc `deleted_at`

### **4. Audit Trail:**
- Tất cả bảng có `created_at`, `updated_at`
- `Order_Status_History` track thay đổi

---

## 💾 BACKUP STRATEGY

### **Daily Backup:**
```sql
-- Full backup
BACKUP DATABASE UTE_Fashion 
TO DISK = 'D:\Backups\UTE_Fashion_Full.bak'
WITH INIT, COMPRESSION;
```

### **Transaction Log Backup:**
```sql
-- Every 1 hour
BACKUP LOG UTE_Fashion
TO DISK = 'D:\Backups\UTE_Fashion_Log.trn'
WITH COMPRESSION;
```

---

## 📊 QUERIES HỮU ÍCH

### **1. Top 10 sản phẩm bán chạy:**
```sql
SELECT TOP 10 
    p.product_name,
    p.sold_count,
    p.average_rating,
    c.category_name
FROM Products p
JOIN Categories c ON p.category_id = c.category_id
WHERE p.is_active = 1
ORDER BY p.sold_count DESC;
```

### **2. Doanh thu theo tháng:**
```sql
SELECT 
    YEAR(order_date) AS year,
    MONTH(order_date) AS month,
    COUNT(*) AS total_orders,
    SUM(total_amount) AS total_revenue
FROM Orders
WHERE payment_status = 'Paid'
GROUP BY YEAR(order_date), MONTH(order_date)
ORDER BY year DESC, month DESC;
```

### **3. Sản phẩm sắp hết hàng:**
```sql
SELECT 
    p.product_name,
    p.stock_quantity,
    p.low_stock_threshold
FROM Products p
WHERE p.stock_quantity <= p.low_stock_threshold
  AND p.is_active = 1
ORDER BY p.stock_quantity ASC;
```

### **4. Khách hàng VIP (mua nhiều nhất):**
```sql
SELECT TOP 10
    u.full_name,
    u.email,
    COUNT(o.order_id) AS total_orders,
    SUM(o.total_amount) AS total_spent
FROM Users u
JOIN Orders o ON u.user_id = o.user_id
WHERE o.payment_status = 'Paid'
GROUP BY u.user_id, u.full_name, u.email
ORDER BY total_spent DESC;
```

### **5. Sản phẩm chưa có review:**
```sql
SELECT 
    p.product_id,
    p.product_name,
    p.sold_count
FROM Products p
LEFT JOIN Reviews r ON p.product_id = r.product_id
WHERE r.review_id IS NULL
  AND p.sold_count > 0
ORDER BY p.sold_count DESC;
```

---

## 🚀 PERFORMANCE OPTIMIZATION

### **1. Denormalization:**
- `Products.average_rating`, `review_count` (tính trước)
- `Products.sold_count` (cập nhật khi order)
- Snapshot price trong `Cart_Items`, `Order_Items`

### **2. Caching Strategy:**
- Cache categories (ít thay đổi)
- Cache featured products
- Cache settings
- Redis cho session management

### **3. Query Optimization:**
- Sử dụng indexes
- Avoid SELECT *
- Pagination cho list views
- Lazy loading cho images

---

## 📝 LƯU Ý QUAN TRỌNG

### **1. Khi thêm sản phẩm vào giỏ:**
- Kiểm tra stock_quantity
- Lưu snapshot price hiện tại

### **2. Khi tạo đơn hàng:**
- Trừ stock_quantity
- Tạo record trong `Inventory_Transactions`
- Cập nhật `sold_count`
- Gửi email notification
- Broadcast WebSocket notification

### **3. Khi thanh toán thành công:**
- Cập nhật `payment_status` = 'Paid'
- Tạo record trong `Payments`
- Gửi email invoice

### **4. Khi user review:**
- Tính lại `average_rating`
- Tăng `review_count`
- Chờ admin approve

### **5. WebSocket Events:**
- Order status changed → Notify user
- New order created → Notify admin
- Low stock alert → Notify admin
- New product → Broadcast all users

---

## 🛠️ MAINTENANCE

### **Daily Tasks:**
- Backup database
- Check log files
- Monitor slow queries

### **Weekly Tasks:**
- Update indexes statistics
- Check for deadlocks
- Review error logs

### **Monthly Tasks:**
- Archive old data (orders > 1 year)
- Optimize database
- Review and clean up unused data

---

## 📖 REFERENCES

**SQL Server Documentation:**
- https://docs.microsoft.com/en-us/sql/

**Best Practices:**
- Normalization vs Denormalization
- Index optimization
- Query performance tuning
- Security best practices

---

**Tạo bởi: UTE Fashion Team**  
**Ngày cập nhật: 2024**  
**Version: 1.0**

