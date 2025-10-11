-- =============================================
-- UTE FASHION - DATABASE SCHEMA
-- Website Shop Thời Trang Online
-- SQL Server Database
-- =============================================

USE master;
GO

-- Tạo database
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'UTE_Fashion')
BEGIN
    ALTER DATABASE UTE_Fashion SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE UTE_Fashion;
END
GO

CREATE DATABASE UTE_Fashion;
GO

USE UTE_Fashion;
GO

-- =============================================
-- BẢNG: USERS & AUTHENTICATION
-- =============================================

-- Bảng Roles (Vai trò)
CREATE TABLE Roles (
    role_id INT PRIMARY KEY IDENTITY(1,1),
    role_name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Users (Người dùng)
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20),
    avatar_url NVARCHAR(500),
    date_of_birth DATE,
    gender NVARCHAR(10), -- 'Male', 'Female', 'Other'
    is_active BIT DEFAULT 1,
    is_email_verified BIT DEFAULT 0,
    email_verification_token NVARCHAR(255),
    password_reset_token NVARCHAR(255),
    password_reset_expires DATETIME,
    last_login DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng User_Roles (Liên kết User và Role - Many to Many)
CREATE TABLE User_Roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE
);

-- Bảng Addresses (Địa chỉ giao hàng)
CREATE TABLE Addresses (
    address_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    recipient_name NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20) NOT NULL,
    address_line1 NVARCHAR(255) NOT NULL,
    address_line2 NVARCHAR(255),
    ward NVARCHAR(100),
    district NVARCHAR(100) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    postal_code NVARCHAR(20),
    is_default BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: CATEGORIES & BRANDS
-- =============================================

-- Bảng Categories (Danh mục sản phẩm - Tree structure)
CREATE TABLE Categories (
    category_id INT PRIMARY KEY IDENTITY(1,1),
    category_name NVARCHAR(100) NOT NULL,
    slug NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    parent_category_id INT NULL,
    image_url NVARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (parent_category_id) REFERENCES Categories(category_id)
);

-- Bảng Brands (Thương hiệu)
CREATE TABLE Brands (
    brand_id INT PRIMARY KEY IDENTITY(1,1),
    brand_name NVARCHAR(100) NOT NULL UNIQUE,
    slug NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    logo_url NVARCHAR(500),
    website_url NVARCHAR(255),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =============================================
-- BẢNG: PRODUCTS
-- =============================================

-- Bảng Products (Sản phẩm)
CREATE TABLE Products (
    product_id INT PRIMARY KEY IDENTITY(1,1),
    product_name NVARCHAR(255) NOT NULL,
    slug NVARCHAR(255) NOT NULL UNIQUE,
    sku NVARCHAR(100) UNIQUE,
    category_id INT NOT NULL,
    brand_id INT,
    description NVARCHAR(MAX),
    short_description NVARCHAR(500),
    price DECIMAL(18,2) NOT NULL,
    sale_price DECIMAL(18,2),
    cost_price DECIMAL(18,2),
    stock_quantity INT DEFAULT 0,
    low_stock_threshold INT DEFAULT 10,
    weight DECIMAL(10,2), -- gram
    dimensions NVARCHAR(50), -- LxWxH (cm)
    material NVARCHAR(255),
    is_featured BIT DEFAULT 0,
    is_new_arrival BIT DEFAULT 0,
    is_best_seller BIT DEFAULT 0,
    is_active BIT DEFAULT 1,
    view_count INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES Brands(brand_id)
);

-- Bảng Product_Images (Hình ảnh sản phẩm)
CREATE TABLE Product_Images (
    image_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    alt_text NVARCHAR(255),
    display_order INT DEFAULT 0,
    is_primary BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- Bảng Sizes (Kích cỡ)
CREATE TABLE Sizes (
    size_id INT PRIMARY KEY IDENTITY(1,1),
    size_name NVARCHAR(20) NOT NULL UNIQUE, -- S, M, L, XL, XXL, 38, 39, 40...
    size_type NVARCHAR(50), -- 'Clothing', 'Shoes'
    display_order INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng Colors (Màu sắc)
CREATE TABLE Colors (
    color_id INT PRIMARY KEY IDENTITY(1,1),
    color_name NVARCHAR(50) NOT NULL UNIQUE,
    color_code NVARCHAR(20), -- Hex code: #FF0000
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng Product_Variants (Biến thể sản phẩm - Size, Color)
CREATE TABLE Product_Variants (
    variant_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL,
    size_id INT,
    color_id INT,
    sku NVARCHAR(100) UNIQUE,
    price_adjustment DECIMAL(18,2) DEFAULT 0,
    stock_quantity INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (size_id) REFERENCES Sizes(size_id),
    FOREIGN KEY (color_id) REFERENCES Colors(color_id),
    UNIQUE (product_id, size_id, color_id)
);

-- =============================================
-- BẢNG: SHOPPING CART
-- =============================================

-- Bảng Carts (Giỏ hàng)
CREATE TABLE Carts (
    cart_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NULL, -- NULL cho guest
    session_id NVARCHAR(255), -- Cho guest users
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Bảng Cart_Items (Sản phẩm trong giỏ hàng)
CREATE TABLE Cart_Items (
    cart_item_id INT PRIMARY KEY IDENTITY(1,1),
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    variant_id INT,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(18,2) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES Carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id)
);

-- =============================================
-- BẢNG: ORDERS
-- =============================================

-- Bảng Orders (Đơn hàng)
CREATE TABLE Orders (
    order_id INT PRIMARY KEY IDENTITY(1,1),
    order_number NVARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    
    -- Shipping Information
    recipient_name NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20) NOT NULL,
    email NVARCHAR(100),
    shipping_address NVARCHAR(500) NOT NULL,
    ward NVARCHAR(100),
    district NVARCHAR(100) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    postal_code NVARCHAR(20),
    
    -- Order Details
    subtotal DECIMAL(18,2) NOT NULL,
    shipping_fee DECIMAL(18,2) DEFAULT 0,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL,
    
    -- Status
    order_status NVARCHAR(50) NOT NULL DEFAULT 'Pending', 
    -- 'Pending', 'Confirmed', 'Processing', 'Shipping', 'Delivered', 'Cancelled', 'Refunded'
    payment_status NVARCHAR(50) NOT NULL DEFAULT 'Unpaid', 
    -- 'Unpaid', 'Paid', 'Refunded', 'Failed'
    
    -- Notes
    customer_notes NVARCHAR(MAX),
    admin_notes NVARCHAR(MAX),
    
    -- Timestamps
    order_date DATETIME DEFAULT GETDATE(),
    confirmed_at DATETIME,
    shipped_at DATETIME,
    delivered_at DATETIME,
    cancelled_at DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng Order_Items (Chi tiết đơn hàng)
CREATE TABLE Order_Items (
    order_item_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    variant_id INT,
    product_name NVARCHAR(255) NOT NULL,
    product_sku NVARCHAR(100),
    size NVARCHAR(20),
    color NVARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    total_price DECIMAL(18,2) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id)
);

-- Bảng Order_Status_History (Lịch sử trạng thái đơn hàng)
CREATE TABLE Order_Status_History (
    history_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    old_status NVARCHAR(50),
    new_status NVARCHAR(50) NOT NULL,
    notes NVARCHAR(MAX),
    changed_by INT, -- user_id của người thay đổi
    changed_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES Users(user_id)
);

-- =============================================
-- BẢNG: PAYMENTS
-- =============================================

-- Bảng Payment_Methods (Phương thức thanh toán)
CREATE TABLE Payment_Methods (
    payment_method_id INT PRIMARY KEY IDENTITY(1,1),
    method_name NVARCHAR(100) NOT NULL UNIQUE,
    method_code NVARCHAR(50) NOT NULL UNIQUE, -- 'COD', 'VNPAY', 'MOMO', 'BANK_TRANSFER'
    description NVARCHAR(500),
    icon_url NVARCHAR(500),
    is_active BIT DEFAULT 1,
    display_order INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng Payments (Thanh toán)
CREATE TABLE Payments (
    payment_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    payment_method_id INT NOT NULL,
    transaction_id NVARCHAR(255), -- ID từ payment gateway
    amount DECIMAL(18,2) NOT NULL,
    payment_status NVARCHAR(50) NOT NULL DEFAULT 'Pending', 
    -- 'Pending', 'Success', 'Failed', 'Refunded'
    payment_gateway_response NVARCHAR(MAX), -- JSON response
    paid_at DATETIME,
    refunded_at DATETIME,
    refund_amount DECIMAL(18,2),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (payment_method_id) REFERENCES Payment_Methods(payment_method_id)
);

-- =============================================
-- BẢNG: COUPONS & DISCOUNTS
-- =============================================

-- Bảng Coupons (Mã giảm giá)
CREATE TABLE Coupons (
    coupon_id INT PRIMARY KEY IDENTITY(1,1),
    coupon_code NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(500),
    discount_type NVARCHAR(20) NOT NULL, -- 'Percentage', 'Fixed'
    discount_value DECIMAL(18,2) NOT NULL,
    min_order_value DECIMAL(18,2) DEFAULT 0,
    max_discount_amount DECIMAL(18,2),
    usage_limit INT, -- Số lần sử dụng tối đa
    usage_count INT DEFAULT 0,
    usage_limit_per_user INT DEFAULT 1,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Coupon_Usage (Lịch sử sử dụng coupon)
CREATE TABLE Coupon_Usage (
    usage_id INT PRIMARY KEY IDENTITY(1,1),
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    order_id INT NOT NULL,
    discount_amount DECIMAL(18,2) NOT NULL,
    used_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (coupon_id) REFERENCES Coupons(coupon_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- =============================================
-- BẢNG: REVIEWS & RATINGS
-- =============================================

-- Bảng Reviews (Đánh giá sản phẩm)
CREATE TABLE Reviews (
    review_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    order_id INT, -- Chỉ người mua mới được review
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title NVARCHAR(255),
    comment NVARCHAR(MAX),
    is_verified_purchase BIT DEFAULT 0,
    is_approved BIT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- Bảng Review_Images (Hình ảnh đánh giá)
CREATE TABLE Review_Images (
    review_image_id INT PRIMARY KEY IDENTITY(1,1),
    review_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (review_id) REFERENCES Reviews(review_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: WISHLIST
-- =============================================

-- Bảng Wishlists (Danh sách yêu thích)
CREATE TABLE Wishlists (
    wishlist_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    added_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- =============================================
-- BẢNG: NOTIFICATIONS (cho WebSocket)
-- =============================================

-- Bảng Notifications (Thông báo)
CREATE TABLE Notifications (
    notification_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NULL, -- NULL = broadcast to all
    notification_type NVARCHAR(50) NOT NULL, 
    -- 'ORDER_STATUS', 'NEW_PRODUCT', 'PROMOTION', 'LOW_STOCK', 'NEW_REVIEW'
    title NVARCHAR(255) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    link_url NVARCHAR(500),
    is_read BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: INVENTORY MANAGEMENT
-- =============================================

-- Bảng Inventory_Transactions (Lịch sử kho hàng)
CREATE TABLE Inventory_Transactions (
    transaction_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL,
    variant_id INT,
    transaction_type NVARCHAR(50) NOT NULL, 
    -- 'IMPORT', 'EXPORT', 'ORDER', 'RETURN', 'ADJUSTMENT'
    quantity_change INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_id INT, -- order_id hoặc reference khác
    notes NVARCHAR(MAX),
    created_by INT,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id),
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- =============================================
-- BẢNG: WEBSITE SETTINGS & BANNERS
-- =============================================

-- Bảng Banners (Banner quảng cáo)
CREATE TABLE Banners (
    banner_id INT PRIMARY KEY IDENTITY(1,1),
    title NVARCHAR(255) NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    link_url NVARCHAR(500),
    description NVARCHAR(500),
    position NVARCHAR(50), -- 'Homepage', 'Sidebar', 'Footer'
    display_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    start_date DATETIME,
    end_date DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Settings (Cấu hình website)
CREATE TABLE Settings (
    setting_id INT PRIMARY KEY IDENTITY(1,1),
    setting_key NVARCHAR(100) NOT NULL UNIQUE,
    setting_value NVARCHAR(MAX),
    setting_type NVARCHAR(50), -- 'String', 'Number', 'Boolean', 'JSON'
    description NVARCHAR(500),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =============================================
-- BẢNG: CONTACT & SUPPORT
-- =============================================

-- Bảng Contacts (Liên hệ)
CREATE TABLE Contacts (
    contact_id INT PRIMARY KEY IDENTITY(1,1),
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20),
    subject NVARCHAR(255) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    status NVARCHAR(50) DEFAULT 'New', -- 'New', 'Processing', 'Resolved'
    replied_at DATETIME,
    created_at DATETIME DEFAULT GETDATE()
);

-- =============================================
-- CREATE INDEXES
-- =============================================

-- Users
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_users_is_active ON Users(is_active);

-- Products
CREATE INDEX idx_products_category ON Products(category_id);
CREATE INDEX idx_products_brand ON Products(brand_id);
CREATE INDEX idx_products_slug ON Products(slug);
CREATE INDEX idx_products_is_active ON Products(is_active);
CREATE INDEX idx_products_created_at ON Products(created_at);

-- Orders
CREATE INDEX idx_orders_user ON Orders(user_id);
CREATE INDEX idx_orders_order_number ON Orders(order_number);
CREATE INDEX idx_orders_status ON Orders(order_status);
CREATE INDEX idx_orders_date ON Orders(order_date);

-- Reviews
CREATE INDEX idx_reviews_product ON Reviews(product_id);
CREATE INDEX idx_reviews_user ON Reviews(user_id);

-- Notifications
CREATE INDEX idx_notifications_user ON Notifications(user_id);
CREATE INDEX idx_notifications_is_read ON Notifications(is_read);

GO

PRINT 'Database UTE_Fashion created successfully!';
GO

