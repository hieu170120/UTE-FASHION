-- =============================================
-- UTE FASHION - DATABASE SCHEMA
-- Website Shop Thời Trang Online
-- SQL Server Database
-- Chú thích: Mỗi bảng được giải thích chức năng và liên kết với các yêu cầu cụ thể
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

-- Bảng Roles: Lưu các vai trò trong hệ thống (ADMIN, USER, VENDOR, SHIPPER, v.v.)
-- Chức năng: Quản lý phân quyền cho các chức năng như đăng nhập, quản lý user, quản lý shop, giao hàng.
CREATE TABLE Roles (
    role_id INT PRIMARY KEY IDENTITY(1,1),
    role_name NVARCHAR(50) NOT NULL UNIQUE, -- Tên vai trò (ADMIN, USER, VENDOR, SHIPPER,...)
    description NVARCHAR(255), -- Mô tả vai trò
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Users: Lưu thông tin người dùng (khách hàng, vendor, shipper, admin,...)
-- Chức năng: Hỗ trợ đăng ký, đăng nhập, quên mật khẩu, quản lý hồ sơ người dùng (profile).
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE, -- Tên đăng nhập
    email NVARCHAR(100) NOT NULL UNIQUE, -- Email để gửi OTP (đăng ký, quên mật khẩu)
    password_hash NVARCHAR(255) NOT NULL, -- Mật khẩu mã hóa (BCrypt)
    full_name NVARCHAR(100) NOT NULL, -- Họ tên người dùng
    phone_number NVARCHAR(20), -- Số điện thoại
    avatar_url NVARCHAR(500), -- Ảnh đại diện
    date_of_birth DATE, -- Ngày sinh
    gender NVARCHAR(10), -- Giới tính
    is_active BIT DEFAULT 1, -- Trạng thái tài khoản (kích hoạt/khóa)
    is_email_verified BIT DEFAULT 0, -- Xác thực email qua OTP
    email_verification_token NVARCHAR(255), -- Mã OTP để xác thực email
    password_reset_token NVARCHAR(255), -- Mã OTP để reset mật khẩu
    password_reset_expires DATETIME, -- Thời gian hết hạn mã reset
    last_login DATETIME, -- Thời gian đăng nhập cuối
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng User_Roles: Liên kết người dùng với vai trò
-- Chức năng: Phân quyền cho user (VD: Vendor có quyền của User + quyền quản lý shop).
CREATE TABLE User_Roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE
);

-- Bảng PasswordResetTokens: Lưu trữ token và OTP để reset mật khẩu
-- Chức năng: Hỗ trợ chức năng quên mật khẩu với OTP qua email.
CREATE TABLE PasswordResetTokens (
    token_id INT PRIMARY KEY IDENTITY(1,1),
    token NVARCHAR(255) NOT NULL UNIQUE, -- Token UUID để xác thực
    otp_code NVARCHAR(6) NOT NULL, -- Mã OTP 6 chữ số
    email NVARCHAR(100) NOT NULL, -- Email của user
    expires_at DATETIME NOT NULL, -- Thời gian hết hạn
    is_used BIT DEFAULT 0, -- Đã sử dụng hay chưa
    created_at DATETIME DEFAULT GETDATE(), -- Thời gian tạo
    used_at DATETIME, -- Thời gian sử dụng
    user_id INT NOT NULL, -- Liên kết với user
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Bảng Addresses: Lưu địa chỉ nhận hàng của người dùng
-- Chức năng: Quản lý địa chỉ trong hồ sơ người dùng, sử dụng khi thanh toán.
CREATE TABLE Addresses (
    address_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL, -- Liên kết với người dùng
    recipient_name NVARCHAR(100) NOT NULL, -- Tên người nhận
    phone_number NVARCHAR(20) NOT NULL, -- Số điện thoại người nhận
    address_line1 NVARCHAR(255) NOT NULL, -- Địa chỉ dòng 1
    address_line2 NVARCHAR(255), -- Địa chỉ dòng 2 (tùy chọn)
    ward NVARCHAR(100), -- Phường/xã
    district NVARCHAR(100) NOT NULL, -- Quận/huyện
    city NVARCHAR(100) NOT NULL, -- Thành phố
    postal_code NVARCHAR(20), -- Mã bưu điện
    is_default BIT DEFAULT 0, -- Địa chỉ mặc định
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: SHOPS (cho Vendor)
-- =============================================

-- Bảng Shops: Lưu thông tin cửa hàng của Vendor
-- Chức năng: Hỗ trợ đăng ký shop, quản lý trang chủ shop, hiển thị sản phẩm của shop.
CREATE TABLE Shops (
    shop_id INT PRIMARY KEY IDENTITY(1,1),
    vendor_id INT NOT NULL, -- Liên kết với user có vai trò VENDOR
    shop_name NVARCHAR(100) NOT NULL UNIQUE, -- Tên cửa hàng
    slug NVARCHAR(100) NOT NULL UNIQUE, -- Đường dẫn SEO-friendly
    description NVARCHAR(500), -- Mô tả cửa hàng
    logo_url NVARCHAR(500), -- Logo cửa hàng
    is_active BIT DEFAULT 0, -- Trạng thái shop (0: chờ duyệt, 1: đã duyệt)
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (vendor_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: CATEGORIES & BRANDS
-- =============================================

-- Bảng Categories: Lưu danh mục sản phẩm (cha và con)
-- Chức năng: Hỗ trợ hiển thị sản phẩm theo danh mục, quản lý danh mục (Admin).
CREATE TABLE Categories (
    category_id INT PRIMARY KEY IDENTITY(1,1),
    category_name NVARCHAR(100) NOT NULL, -- Tên danh mục
    slug NVARCHAR(100) NOT NULL UNIQUE, -- Đường dẫn SEO-friendly
    description NVARCHAR(500), -- Mô tả danh mục
    parent_category_id INT NULL, -- Danh mục cha (hỗ trợ danh mục con)
    image_url NVARCHAR(500), -- Hình ảnh danh mục
    display_order INT DEFAULT 0, -- Thứ tự hiển thị
    is_active BIT DEFAULT 1, -- Trạng thái danh mục
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (parent_category_id) REFERENCES Categories(category_id)
);

-- Bảng Brands: Lưu thông tin thương hiệu
-- Chức năng: Hỗ trợ lọc sản phẩm theo thương hiệu, quản lý thương hiệu (Admin).
CREATE TABLE Brands (
    brand_id INT PRIMARY KEY IDENTITY(1,1),
    brand_name NVARCHAR(100) NOT NULL UNIQUE, -- Tên thương hiệu
    slug NVARCHAR(100) NOT NULL UNIQUE, -- Đường dẫn SEO-friendly
    description NVARCHAR(500), -- Mô tả thương hiệu
    logo_url NVARCHAR(500), -- Logo thương hiệu
    website_url NVARCHAR(255), -- Website thương hiệu
    is_active BIT DEFAULT 1, -- Trạng thái thương hiệu
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =============================================
-- BẢNG: PRODUCTS
-- =============================================

-- Bảng Products: Lưu thông tin sản phẩm
-- Chức năng: Hỗ trợ hiển thị sản phẩm (trang chủ, danh mục, chi tiết), tìm kiếm, lọc, quản lý sản phẩm (Vendor, Admin).
CREATE TABLE Products (
    product_id INT PRIMARY KEY IDENTITY(1,1),
    product_name NVARCHAR(255) NOT NULL, -- Tên sản phẩm
    slug NVARCHAR(255) NOT NULL UNIQUE, -- Đường dẫn SEO-friendly
    sku NVARCHAR(100) UNIQUE, -- Mã sản phẩm
    category_id INT NOT NULL, -- Liên kết danh mục
    brand_id INT, -- Liên kết thương hiệu
    shop_id INT, -- Liên kết với Vendor
    description NVARCHAR(MAX), -- Mô tả chi tiết
    short_description NVARCHAR(500), -- Mô tả ngắn
    price DECIMAL(18,2) NOT NULL, -- Giá gốc
    sale_price DECIMAL(18,2), -- Giá khuyến mãi
    cost_price DECIMAL(18,2), -- Giá vốn
    stock_quantity INT DEFAULT 0, -- Số lượng tồn kho
    low_stock_threshold INT DEFAULT 10, -- Ngưỡng tồn kho thấp
    weight DECIMAL(10,2), -- Trọng lượng
    dimensions NVARCHAR(50), -- Kích thước
    material NVARCHAR(255), -- Chất liệu
    is_featured BIT DEFAULT 0, -- Sản phẩm nổi bật (trang chủ)
    is_new_arrival BIT DEFAULT 0, -- Sản phẩm mới
    is_best_seller BIT DEFAULT 0, -- Sản phẩm bán chạy (>10 lượt bán)
    is_active BIT DEFAULT 1, -- Trạng thái sản phẩm
    view_count INT DEFAULT 0, -- Số lượt xem
    sold_count INT DEFAULT 0, -- Số lượt bán
    average_rating DECIMAL(3,2) DEFAULT 0, -- Điểm đánh giá trung bình
    review_count INT DEFAULT 0, -- Số lượng đánh giá
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES Brands(brand_id),
	FOREIGN KEY (shop_id) REFERENCES Shops(shop_id) 
);

-- Bảng Product_Images: Lưu hình ảnh sản phẩm
-- Chức năng: Hiển thị hình ảnh trong trang chi tiết sản phẩm.
CREATE TABLE Product_Images (
    image_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL, -- Liên kết sản phẩm
    image_url NVARCHAR(500) NOT NULL, -- URL hình ảnh
    alt_text NVARCHAR(255), -- Văn bản thay thế
    display_order INT DEFAULT 0, -- Thứ tự hiển thị
    is_primary BIT DEFAULT 0, -- Hình ảnh chính
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- Bảng Sizes: Lưu kích cỡ sản phẩm
-- Chức năng: Hỗ trợ biến thể sản phẩm (size).
CREATE TABLE Sizes (
    size_id INT PRIMARY KEY IDENTITY(1,1),
    size_name NVARCHAR(20) NOT NULL UNIQUE, -- Tên kích cỡ (S, M, L,...)
    size_type NVARCHAR(50), -- Loại kích cỡ (Clothing, Shoes,...)
    display_order INT DEFAULT 0, -- Thứ tự hiển thị
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng Colors: Lưu màu sắc sản phẩm
-- Chức năng: Hỗ trợ biến thể sản phẩm (màu).
CREATE TABLE Colors (
    color_id INT PRIMARY KEY IDENTITY(1,1),
    color_name NVARCHAR(50) NOT NULL UNIQUE, -- Tên màu
    color_code NVARCHAR(20), -- Mã màu (hex)
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng Product_Variants: Lưu biến thể sản phẩm (kết hợp size, màu)
-- Chức năng: Hỗ trợ hiển thị biến thể trong chi tiết sản phẩm, giỏ hàng, đơn hàng.
CREATE TABLE Product_Variants (
    variant_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL, -- Liên kết sản phẩm
    size_id INT, -- Liên kết kích cỡ
    color_id INT, -- Liên kết màu sắc
    sku NVARCHAR(100) UNIQUE, -- Mã biến thể
    price_adjustment DECIMAL(18,2) DEFAULT 0, -- Điều chỉnh giá
    stock_quantity INT DEFAULT 0, -- Số lượng tồn kho
    is_active BIT DEFAULT 1, -- Trạng thái biến thể
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (size_id) REFERENCES Sizes(size_id),
    FOREIGN KEY (color_id) REFERENCES Colors(color_id),
    UNIQUE (product_id, size_id, color_id)
);

-- Bảng Viewed_Products: Lưu lịch sử xem sản phẩm của người dùng
-- Chức năng: Hỗ trợ hiển thị danh sách sản phẩm đã xem.
CREATE TABLE Viewed_Products (
    view_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL, -- Liên kết người dùng
    product_id INT NOT NULL, -- Liên kết sản phẩm
    viewed_at DATETIME DEFAULT GETDATE(), -- Thời gian xem
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- =============================================
-- BẢNG: SHOPPING CART
-- =============================================

-- Bảng Carts: Lưu giỏ hàng của người dùng hoặc khách (guest)
-- Chức năng: Quản lý giỏ hàng, hỗ trợ lưu trữ cho cả user đăng nhập và guest.
CREATE TABLE Carts (
    cart_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NULL, -- Liên kết với người dùng (NULL cho guest)
    session_id NVARCHAR(255), -- Mã phiên cho guest
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Bảng Cart_Items: Lưu chi tiết sản phẩm trong giỏ hàng
-- Chức năng: Hỗ trợ thêm/xóa/cập nhật sản phẩm trong giỏ hàng.
CREATE TABLE Cart_Items (
    cart_item_id INT PRIMARY KEY IDENTITY(1,1),
    cart_id INT NOT NULL, -- Liên kết giỏ hàng
    product_id INT NOT NULL, -- Liên kết sản phẩm
    variant_id INT, -- Liên kết biến thể
    quantity INT NOT NULL DEFAULT 1, -- Số lượng
    price DECIMAL(18,2) NOT NULL, -- Giá tại thời điểm thêm
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES Carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id)
);

-- =============================================
-- BẢNG: CARRIERS & SHIPPERS
-- =============================================

-- Bảng Carriers: Lưu thông tin nhà vận chuyển
-- Chức năng: Quản lý nhà vận chuyển (Admin), áp dụng phí vận chuyển.
CREATE TABLE Carriers (
    carrier_id INT PRIMARY KEY IDENTITY(1,1),
    carrier_name NVARCHAR(100) NOT NULL UNIQUE, -- Tên nhà vận chuyển
    description NVARCHAR(500), -- Mô tả
    default_shipping_fee DECIMAL(18,2) DEFAULT 0, -- Phí vận chuyển mặc định
    contact_phone NVARCHAR(20), -- Số điện thoại
    website_url NVARCHAR(255), -- Website
    is_active BIT DEFAULT 1, -- Trạng thái
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Shippers: Lưu thông tin nhân viên giao hàng
-- Chức năng: Hỗ trợ phân công đơn hàng cho shipper, thống kê đơn hàng.
CREATE TABLE Shippers (
    shipper_id INT PRIMARY KEY IDENTITY(1,1),
    carrier_id INT, -- Liên kết nhà vận chuyển
    user_id INT, -- Liên kết với tài khoản user (vai trò SHIPPER)
    full_name NVARCHAR(100) NOT NULL, -- Họ tên shipper
    phone_number NVARCHAR(20) NOT NULL, -- Số điện thoại
    email NVARCHAR(100), -- Email
    vehicle_type NVARCHAR(50), -- Loại phương tiện
    is_active BIT DEFAULT 1, -- Trạng thái
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (carrier_id) REFERENCES Carriers(carrier_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

-- Bảng Carrier_Shipper_Fees: Lưu phí vận chuyển riêng cho từng shipper
-- Chức năng: Quản lý phí vận chuyển cá nhân hóa (Admin).
CREATE TABLE Carrier_Shipper_Fees (
    fee_id INT PRIMARY KEY IDENTITY(1,1),
    carrier_id INT NOT NULL, -- Liên kết nhà vận chuyển
    shipper_id INT NOT NULL, -- Liên kết shipper
    shipping_fee DECIMAL(18,2) NOT NULL DEFAULT 0, -- Phí vận chuyển
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (carrier_id) REFERENCES Carriers(carrier_id) ON DELETE CASCADE,
    FOREIGN KEY (shipper_id) REFERENCES Shippers(shipper_id) ON DELETE CASCADE,
    UNIQUE (carrier_id, shipper_id)
);

-- =============================================
-- BẢNG: ORDERS
-- =============================================

-- Bảng Orders: Lưu thông tin đơn hàng
-- Chức năng: Quản lý lịch sử mua hàng (User), quản lý đơn hàng (Vendor, Shipper, Admin).
CREATE TABLE Orders (
    order_id INT PRIMARY KEY IDENTITY(1,1),
    order_number NVARCHAR(50) NOT NULL UNIQUE, -- Mã đơn hàng
    user_id INT NOT NULL, -- Liên kết người mua
    carrier_id INT, -- Liên kết nhà vận chuyển
    shipper_id INT, -- Liên kết shipper
    shop_id INT, 
    shipping_time INT, -- Thời gian giao hàng dự kiến (phút)
    recipient_name NVARCHAR(100) NOT NULL, -- Tên người nhận
    phone_number NVARCHAR(20) NOT NULL, -- Số điện thoại người nhận
    email NVARCHAR(100), -- Email người nhận
    shipping_address NVARCHAR(500) NOT NULL, -- Địa chỉ giao hàng
    ward NVARCHAR(100), -- Phường/xã
    district NVARCHAR(100) NOT NULL, -- Quận/huyện
    city NVARCHAR(100) NOT NULL, -- Thành phố
    postal_code NVARCHAR(20), -- Mã bưu điện
    subtotal DECIMAL(18,2) NOT NULL, -- Tổng giá sản phẩm
    shipping_fee DECIMAL(18,2) DEFAULT 0, -- Phí vận chuyển
    discount_amount DECIMAL(18,2) DEFAULT 0, -- Số tiền giảm giá
    tax_amount DECIMAL(18,2) DEFAULT 0, -- Thuế
    total_amount DECIMAL(18,2) NOT NULL, -- Tổng tiền đơn hàng
    order_status NVARCHAR(50) NOT NULL DEFAULT 'Pending', -- Trạng thái đơn hàng (Pending, Confirmed, Accepted, Shipping, Delivered, Cancelled)
    payment_status NVARCHAR(50) NOT NULL DEFAULT 'Unpaid', -- Trạng thái thanh toán
    customer_notes NVARCHAR(MAX), -- Ghi chú khách hàng
    admin_notes NVARCHAR(MAX), -- Ghi chú admin
    order_date DATETIME DEFAULT GETDATE(), -- Ngày đặt hàng
    confirmed_at DATETIME, -- Thời gian Admin xác nhận đơn hàng
    accepted_at DATETIME, -- ⭐ MỚI: Thời gian Shipper xác nhận nhận đơn (sau khi Admin phân công)
    shipped_at DATETIME, -- Thời gian Shipper bắt đầu giao hàng
    estimated_delivery_time DATETIME, -- ⭐ MỚI: Thời gian dự kiến giao xong (tính từ shipped_at + random 2-5 phút)
    delivered_at DATETIME, -- Thời gian giao hàng thành công
    cancelled_at DATETIME, -- Thời gian hủy đơn
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (carrier_id) REFERENCES Carriers(carrier_id),
    FOREIGN KEY (shipper_id) REFERENCES Shippers(shipper_id),
    FOREIGN KEY (shop_id)	 REFERENCES Shops(shop_id)
);

-- Bảng Order_Items: Lưu chi tiết sản phẩm trong đơn hàng
-- Chức năng: Hỗ trợ hiển thị chi tiết đơn hàng, tính doanh thu Vendor.
CREATE TABLE Order_Items (
    order_item_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL, -- Liên kết đơn hàng
    product_id INT NOT NULL, -- Liên kết sản phẩm
    variant_id INT, -- Liên kết biến thể
    product_name NVARCHAR(255) NOT NULL, -- Tên sản phẩm
    product_sku NVARCHAR(100), -- Mã sản phẩm
    size NVARCHAR(20), -- Kích cỡ
    color NVARCHAR(50), -- Màu sắc
    quantity INT NOT NULL, -- Số lượng
    unit_price DECIMAL(18,2) NOT NULL, -- Giá đơn vị
    total_price DECIMAL(18,2) NOT NULL, -- Tổng giá
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id)
);

-- Bảng Order_Status_History: Lưu lịch sử thay đổi trạng thái đơn hàng
-- Chức năng: Theo dõi các thay đổi trạng thái đơn hàng (Admin, Vendor, Shipper).
CREATE TABLE Order_Status_History (
    history_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL, -- Liên kết đơn hàng
    old_status NVARCHAR(50), -- Trạng thái cũ
    new_status NVARCHAR(50) NOT NULL, -- Trạng thái mới
    notes NVARCHAR(MAX), -- Ghi chú
    changed_by INT, -- Người thay đổi (Admin, Vendor,...)
    changed_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES Users(user_id)
);

-- Bảng Return_Requests: Lưu yêu cầu trả hàng/hoàn tiền
-- Chức năng: Hỗ trợ quản lý trả hàng/hoàn tiền (User, Vendor, Admin).
CREATE TABLE Return_Requests (
    request_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL, -- Liên kết đơn hàng
    user_id INT NOT NULL, -- Liên kết người yêu cầu
    reason NVARCHAR(MAX) NOT NULL, -- Lý do trả hàng
    status NVARCHAR(50) DEFAULT 'Pending', -- Trạng thái (Pending, Approved, Rejected)
    requested_at DATETIME DEFAULT GETDATE(), -- Thời gian yêu cầu
    approved_at DATETIME, -- Thời gian duyệt
    rejected_at DATETIME, -- Thời gian từ chối
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- =============================================
-- BẢNG: PAYMENTS & COUPONS
-- =============================================

-- Bảng Payment_Methods: Lưu phương thức thanh toán
-- Chức năng: Hỗ trợ thanh toán (COD, VNPAY, MOMO).
CREATE TABLE Payment_Methods (
        payment_method_id INT PRIMARY KEY IDENTITY(1,1),
        method_name NVARCHAR(100) NOT NULL UNIQUE,
        method_code NVARCHAR(50) NOT NULL UNIQUE, -- 'COD', 'SEPAY_QR', 'VNPAY', 'MOMO'
        description NVARCHAR(500),
        icon_url NVARCHAR(500),
        is_active BIT DEFAULT 1,
        display_order INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );

-- Bảng Payments: Lưu thông tin thanh toán
-- Chức năng: Quản lý thanh toán và hoàn tiền cho đơn hàng.
CREATE TABLE Payments (
        payment_id INT PRIMARY KEY IDENTITY(1,1),
        order_id INT NOT NULL,
        payment_method_id INT NOT NULL,
        transaction_id NVARCHAR(255), -- ID từ payment gateway
        amount DECIMAL(18,2) NOT NULL,
        payment_status NVARCHAR(50) NOT NULL DEFAULT 'Pending', 
        -- 'Pending', 'Success', 'Failed', 'Refunded'
        payment_gateway_response NVARCHAR(MAX), -- JSON response từ SePay
        paid_at DATETIME,
        refunded_at DATETIME,
        refund_amount DECIMAL(18,2),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
        FOREIGN KEY (payment_method_id) REFERENCES Payment_Methods(payment_method_id)
    );

-- Bảng Coupons: Lưu mã giảm giá
-- Chức năng: Hỗ trợ áp dụng mã giảm giá (Vendor, Admin), bao gồm giảm giá sản phẩm và phí vận chuyển.
CREATE TABLE Coupons (
    coupon_id INT PRIMARY KEY IDENTITY(1,1),
    coupon_code NVARCHAR(50) NOT NULL UNIQUE, -- Mã giảm giá
    description NVARCHAR(500), -- Mô tả
    discount_type NVARCHAR(20) NOT NULL, -- Loại giảm giá (Percentage, Fixed)
    discount_value DECIMAL(18,2) NOT NULL, -- Giá trị giảm
    min_order_value DECIMAL(18,2) DEFAULT 0, -- Giá trị đơn hàng tối thiểu
    max_discount_amount DECIMAL(18,2), -- Giảm giá tối đa
    vendor_id INT, -- Liên kết Vendor (NULL nếu là mã của Admin)
    usage_limit INT, -- Giới hạn sử dụng
    usage_count INT DEFAULT 0, -- Số lần đã sử dụng
    usage_limit_per_user INT DEFAULT 1, -- Giới hạn sử dụng mỗi người dùng
    valid_from DATETIME NOT NULL, -- Thời gian bắt đầu
    valid_to DATETIME NOT NULL, -- Thời gian kết thúc
    is_active BIT DEFAULT 1, -- Trạng thái
    is_shipping_discount BIT DEFAULT 0, -- Mã giảm phí vận chuyển
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (vendor_id) REFERENCES Users(user_id)
);

-- Bảng Coupon_Usage: Lưu lịch sử sử dụng mã giảm giá
-- Chức năng: Theo dõi việc áp dụng mã giảm giá cho đơn hàng.
CREATE TABLE Coupon_Usage (
    usage_id INT PRIMARY KEY IDENTITY(1,1),
    coupon_id INT NOT NULL, -- Liên kết mã giảm giá
    user_id INT NOT NULL, -- Liên kết người dùng
    order_id INT NOT NULL, -- Liên kết đơn hàng
    discount_amount DECIMAL(18,2) NOT NULL, -- Số tiền giảm
    used_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (coupon_id) REFERENCES Coupons(coupon_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- Bảng App_Discounts: Lưu chiết khấu của ứng dụng cho shop
-- Chức năng: Quản lý chiết khấu do Admin tạo cho shop.
CREATE TABLE App_Discounts (
    discount_id INT PRIMARY KEY IDENTITY(1,1),
    shop_id INT NOT NULL, -- Liên kết shop
    discount_type NVARCHAR(20) NOT NULL, -- Loại chiết khấu (Percentage, Fixed)
    discount_value DECIMAL(18,2) NOT NULL, -- Giá trị chiết khấu
    description NVARCHAR(500), -- Mô tả
    valid_from DATETIME NOT NULL, -- Thời gian bắt đầu
    valid_to DATETIME NOT NULL, -- Thời gian kết thúc
    is_active BIT DEFAULT 1, -- Trạng thái
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (shop_id) REFERENCES Shops(shop_id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG: REVIEWS & WISHLISTS
-- =============================================

-- Bảng Reviews: Lưu đánh giá và bình luận sản phẩm
-- Chức năng: Hỗ trợ đánh giá/bình luận sản phẩm đã mua (tối thiểu 50 ký tự, kiểm tra trong code).
CREATE TABLE Reviews (
    review_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL, -- Liên kết sản phẩm
    user_id INT NOT NULL, -- Liên kết người dùng
    order_id INT, -- Liên kết đơn hàng (để kiểm tra is_verified_purchase)
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), -- Điểm đánh giá
    title NVARCHAR(255), -- Tiêu đề đánh giá
    comment NVARCHAR(MAX), -- Nội dung đánh giá/bình luận
    is_verified_purchase BIT DEFAULT 0, -- Đã mua sản phẩm
    is_approved BIT DEFAULT 0, -- Được duyệt bởi Admin
    helpful_count INT DEFAULT 0, -- Số lượt đánh giá hữu ích
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- Bảng Review_Images: Lưu hình ảnh và video trong đánh giá
-- Chức năng: Hỗ trợ đánh giá có hình ảnh/video.
CREATE TABLE Review_Images (
    review_image_id INT PRIMARY KEY IDENTITY(1,1),
    review_id INT NOT NULL, -- Liên kết đánh giá
    image_url NVARCHAR(500) NOT NULL, -- URL hình ảnh
    video_url NVARCHAR(500), -- URL video
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (review_id) REFERENCES Reviews(review_id) ON DELETE CASCADE
);

-- Bảng Wishlists: Lưu danh sách sản phẩm yêu thích
-- Chức năng: Hỗ trợ chức năng "thích sản phẩm" của User.
CREATE TABLE Wishlists (
    wishlist_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL, -- Liên kết người dùng
    product_id INT NOT NULL, -- Liên kết sản phẩm
    added_at DATETIME DEFAULT GETDATE(), -- Thời gian thêm
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- =============================================
-- BẢNG: NOTIFICATIONS & INVENTORY
-- =============================================

-- Bảng Notifications: Lưu thông báo cho người dùng
-- Chức năng: Gửi thông báo về đơn hàng, khuyến mãi, trạng thái shop, v.v.
CREATE TABLE Notifications (
    notification_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NULL, -- Liên kết người dùng (NULL cho thông báo chung)
    notification_type NVARCHAR(50) NOT NULL, -- Loại thông báo
    title NVARCHAR(255) NOT NULL, -- Tiêu đề
    message NVARCHAR(MAX) NOT NULL, -- Nội dung
    link_url NVARCHAR(500), -- Liên kết liên quan
    is_read BIT DEFAULT 0, -- Trạng thái đọc
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Bảng Inventory_Transactions: Lưu giao dịch tồn kho
-- Chức năng: Theo dõi thay đổi tồn kho khi thêm/sửa sản phẩm hoặc đặt hàng.
CREATE TABLE Inventory_Transactions (
    transaction_id INT PRIMARY KEY IDENTITY(1,1),
    product_id INT NOT NULL, -- Liên kết sản phẩm
    variant_id INT, -- Liên kết biến thể
    transaction_type NVARCHAR(50) NOT NULL, -- Loại giao dịch (Add, Remove,...)
    quantity_change INT NOT NULL, -- Số lượng thay đổi
    quantity_before INT NOT NULL, -- Tồn kho trước
    quantity_after INT NOT NULL, -- Tồn kho sau
    reference_id INT, -- ID tham chiếu (VD: order_id)
    notes NVARCHAR(MAX), -- Ghi chú
    created_by INT, -- Người thực hiện
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id),
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- =============================================
-- BẢNG: BANNERS & SETTINGS
-- =============================================

-- Bảng Banners: Lưu thông tin banner trên trang chủ
-- Chức năng: Hiển thị banner quảng cáo trên trang chủ.
CREATE TABLE Banners (
    banner_id INT PRIMARY KEY IDENTITY(1,1),
    title NVARCHAR(255) NOT NULL, -- Tiêu đề banner
    image_url NVARCHAR(500) NOT NULL, -- URL hình ảnh
    link_url NVARCHAR(500), -- Liên kết khi click
    description NVARCHAR(500), -- Mô tả
    position NVARCHAR(50), -- Vị trí hiển thị
    display_order INT DEFAULT 0, -- Thứ tự hiển thị
    is_active BIT DEFAULT 1, -- Trạng thái
    start_date DATETIME, -- Thời gian bắt đầu
    end_date DATETIME, -- Thời gian kết thúc
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Settings: Lưu cấu hình hệ thống
-- Chức năng: Lưu các thiết lập như cấu hình email, API thanh toán, v.v.
CREATE TABLE Settings (
    setting_id INT PRIMARY KEY IDENTITY(1,1),
    setting_key NVARCHAR(100) NOT NULL UNIQUE, -- Tên cấu hình
    setting_value NVARCHAR(MAX), -- Giá trị cấu hình
    setting_type NVARCHAR(50), -- Loại cấu hình
    description NVARCHAR(500), -- Mô tả
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng Contacts: Lưu thông tin liên hệ từ khách hàng (HỦY)
-- Chức năng: Quản lý phản hồi từ khách hàng (hỗ trợ, khiếu nại).
-- CREATE TABLE Contacts (
--     contact_id INT PRIMARY KEY IDENTITY(1,1),
--     full_name NVARCHAR(100) NOT NULL-- Tên người liên hệ, 
--     email NVARCHAR(100) NOT NULL, -- Email
--     phone_number NVARCHAR(20), -- Số điện thoại
--     subject NVARCHAR(255) NOT NULL, -- Chủ đề
--     message NVARCHAR(MAX) NOT NULL, -- Nội dung
--     status NVARCHAR(50) DEFAULT 'New', -- Trạng thái
--     replied_at DATETIME, -- Thời gian phản hồi
--     created_at DATETIME DEFAULT GETDATE()
-- );
-- =============================================
-- LIVE CHAT SYSTEM
-- Chức năng: Cho phép người dùng đã đăng nhập trò chuyện trực tiếp với shop.
-- =============================================

-- Bảng Conversations: Mỗi dòng là một cuộc hội thoại duy nhất giữa một User và một Shop.
-- Mục đích: Nhóm các tin nhắn lại với nhau, quản lý trạng thái và định tuyến.
CREATE TABLE Conversations (
    conversation_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,  -- Người dùng (khách hàng) bắt đầu cuộc hội thoại
    shop_id INT NOT NULL,  -- Shop (vendor) tham gia cuộc hội thoại
    status NVARCHAR(50) DEFAULT 'open' NOT NULL, -- Trạng thái cuộc hội thoại: 'open', 'closed'
    created_at DATETIME DEFAULT GETDATE(), -- Thời điểm bắt đầu
    updated_at DATETIME DEFAULT GETDATE(), -- Thời điểm có tin nhắn cuối cùng

    -- Khóa ngoại trỏ đến người dùng và shop
    CONSTRAINT FK_Conversations_Users FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Conversations_Shops FOREIGN KEY (shop_id) REFERENCES Shops(shop_id),
    -- Đảm bảo mỗi cặp (user, shop) chỉ có một cuộc hội thoại mở tại một thời điểm
    CONSTRAINT UQ_User_Shop_Conversation UNIQUE (user_id, shop_id)
);

-- Bảng Messages: Lưu trữ từng tin nhắn cụ thể trong một cuộc hội thoại.
-- Mục đích: Ghi lại lịch sử trao đổi chi tiết.
CREATE TABLE Messages (
    message_id BIGINT PRIMARY KEY IDENTITY(1,1),
    conversation_id INT NOT NULL, -- Tin nhắn này thuộc về cuộc hội thoại nào
    sender_id INT NOT NULL,       -- ID của người gửi (tham chiếu đến Users.user_id)
    sender_type NVARCHAR(50) NOT NULL, -- 'USER' hoặc 'VENDOR' để xác định vai trò người gửi
    message_content NVARCHAR(MAX) NOT NULL, -- Nội dung tin nhắn
    is_read BIT DEFAULT 0,        -- Trạng thái 'đã đọc' của tin nhắn
    created_at DATETIME DEFAULT GETDATE(), -- Thời gian gửi

    -- Khóa ngoại trỏ đến bảng Conversations và bảng Users (cho người gửi)
    CONSTRAINT FK_Messages_Conversations FOREIGN KEY (conversation_id) REFERENCES Conversations(conversation_id),
    CONSTRAINT FK_Messages_Sender FOREIGN KEY (sender_id) REFERENCES Users(user_id)
);

-- =============================================
-- CREATE INDEXES
-- =============================================

CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_users_is_active ON Users(is_active);
CREATE INDEX idx_products_category ON Products(category_id);
CREATE INDEX idx_products_brand ON Products(brand_id);
CREATE INDEX idx_products_shop ON Products(shop_id);
CREATE INDEX idx_products_slug ON Products(slug);
CREATE INDEX idx_products_is_active ON Products(is_active);
CREATE INDEX idx_products_created_at ON Products(created_at);
CREATE INDEX idx_orders_user ON Orders(user_id);
CREATE INDEX idx_orders_order_number ON Orders(order_number);
CREATE INDEX idx_orders_status ON Orders(order_status);
CREATE INDEX idx_orders_date ON Orders(order_date);
CREATE INDEX idx_orders_carrier ON Orders(carrier_id);
CREATE INDEX idx_orders_shipper ON Orders(shipper_id);
CREATE INDEX idx_reviews_product ON Reviews(product_id);
CREATE INDEX idx_reviews_user ON Reviews(user_id);
CREATE INDEX idx_notifications_user ON Notifications(user_id);
CREATE INDEX idx_notifications_is_read ON Notifications(is_read);
CREATE INDEX idx_carrier_shipper_fees_carrier ON Carrier_Shipper_Fees(carrier_id);
CREATE INDEX idx_carrier_shipper_fees_shipper ON Carrier_Shipper_Fees(shipper_id);
CREATE INDEX idx_shops_vendor ON Shops(vendor_id);
CREATE INDEX idx_app_discounts_shop ON App_Discounts(shop_id);
CREATE INDEX idx_viewed_products_user ON Viewed_Products(user_id);

GO

PRINT 'Database UTE_Fashion created successfully!';
GO
