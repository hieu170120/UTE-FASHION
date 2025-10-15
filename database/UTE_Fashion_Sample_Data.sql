$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
USE UTE_Fashion;
GO

-- =====================================================
-- SEED ROLES & USERS
-- =====================================================
INSERT INTO Roles (role_name, description) VALUES
('ADMIN', N'Quản trị hệ thống'),
('USER', N'Khách hàng thông thường'),
('VENDOR', N'Người bán hàng'),
('SHIPPER', N'Nhân viên giao hàng');

INSERT INTO Users (username, email, password_hash, full_name, phone_number, gender, is_email_verified)
VALUES
('admin', 'admin@ute.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Quản Trị', '0909000001', 'Nam', 1),
('vendor01', 'vendor@ute.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Bán Hàng', '0909000002', 'Nam', 1),
('shipper01', 'shipper@ute.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Giao Hàng', '0909000003', 'Nam', 1),
('user01', 'user01@ute.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Khách Hàng', '0909000004', 'Nữ', 1),
('user02', 'user02@ute.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Người Dùng', '0909000005', 'Nữ', 1);

-- Gán vai trò
INSERT INTO User_Roles (user_id, role_id) VALUES
(1, 1), -- admin
(2, 3), -- vendor
(3, 4), -- shipper
(4, 2), -- user
(5, 2); -- user

-- =====================================================
-- SEED ADDRESSES
-- =====================================================
INSERT INTO Addresses (user_id, recipient_name, phone_number, address_line1, district, city, is_default)
VALUES
(4, N'Phạm Khách Hàng', '0909000004', N'123 Lê Văn Việt', N'Thủ Đức', N'TP.HCM', 1),
(5, N'Nguyễn Người Dùng', '0909000005', N'456 Nguyễn Huệ', N'Quận 1', N'TP.HCM', 1);

-- =====================================================
-- SEED SHOPS
-- =====================================================
INSERT INTO Shops (vendor_id, shop_name, slug, description, logo_url, is_active)
VALUES
(2, N'UTE Style', 'ute-style', N'Cửa hàng thời trang sinh viên UTE', 'https://example.com/logo1.png', 1);

-- =====================================================
-- SEED CATEGORIES & BRANDS
-- =====================================================
INSERT INTO Categories (category_name, slug, description)
VALUES
(N'Áo', 'ao', N'Danh mục các loại áo'),
(N'Quần', 'quan', N'Danh mục các loại quần'),
(N'Phụ kiện', 'phu-kien', N'Các loại phụ kiện thời trang');

INSERT INTO Brands (brand_name, slug, description, logo_url)
VALUES
(N'Nike', 'nike', N'Thương hiệu thể thao nổi tiếng', 'https://example.com/nike.png'),
(N'Zara', 'zara', N'Thương hiệu thời trang cao cấp', 'https://example.com/zara.png'),
(N'Yame', 'yame', N'Thời trang Việt Nam dành cho giới trẻ', 'https://example.com/yame.png');

-- =====================================================
-- SEED PRODUCTS
-- =====================================================
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, shop_id, description, short_description, price, sale_price, stock_quantity, is_new_arrival)
VALUES
(N'Áo Thun UTE Classic', 'ao-thun-ute-classic', 'SKU001', 1, 3, 1, N'Áo thun cotton 100%, in logo UTE', N'Áo thun sinh viên UTE', 120000, 99000, 50, 1),
(N'Quần Jean Xanh', 'quan-jean-xanh', 'SKU002', 2, 2, 1, N'Quần jean co giãn, phù hợp mọi dáng', N'Quần jean Zara', 350000, NULL, 40, 0),
(N'Mũ Lưỡi Trai Đen', 'mu-luoi-trai-den', 'SKU003', 3, 1, 1, N'Mũ đen thời trang, chống nắng', N'Mũ đen Nike', 150000, 125000, 30, 1);

-- =====================================================
-- SEED PRODUCT IMAGES
-- =====================================================
INSERT INTO Product_Images (product_id, image_url, alt_text, is_primary)
VALUES
(1, 'https://example.com/ao1.png', 'Áo thun UTE Classic', 1),
(2, 'https://example.com/quan1.png', 'Quần Jean Xanh', 1),
(3, 'https://example.com/mu1.png', 'Mũ Lưỡi Trai Đen', 1);

-- =====================================================
-- SEED SIZES & COLORS
-- =====================================================
INSERT INTO Sizes (size_name, size_type) VALUES ('S', 'Clothing'), ('M', 'Clothing'), ('L', 'Clothing');
INSERT INTO Colors (color_name, color_code) VALUES (N'Trắng', '#FFFFFF'), (N'Xanh', '#0000FF'), (N'Đen', '#000000');

-- =====================================================
-- SEED PRODUCT VARIANTS
-- =====================================================
INSERT INTO Product_Variants (product_id, size_id, color_id, sku, price_adjustment, stock_quantity)
VALUES
(1, 2, 3, 'SKU001-M-ĐEN', 0, 20),
(2, 3, 2, 'SKU002-L-XANH', 0, 15),
(3, NULL, 3, 'SKU003-ĐEN', 0, 30);

-- =====================================================
-- SEED CARRIERS & SHIPPERS
-- =====================================================
INSERT INTO Carriers (carrier_name, description, default_shipping_fee)
VALUES
(N'Giao Hàng Nhanh', N'Dịch vụ giao hàng nhanh toàn quốc', 25000),
(N'Giao Hàng Tiết Kiệm', N'Dịch vụ giao hàng tiết kiệm', 20000);

INSERT INTO Shippers (carrier_id, user_id, full_name, phone_number, email, vehicle_type)
VALUES
(1, 3, N'Trần Giao Hàng', '0909000003', 'shipper@ute.vn', N'Xe máy');

INSERT INTO Carrier_Shipper_Fees (carrier_id, shipper_id, shipping_fee)
VALUES
(1, 1, 25000);

-- =====================================================
-- SEED ORDERS
-- =====================================================
INSERT INTO Orders (order_number, user_id, carrier_id, shipper_id, recipient_name, phone_number, shipping_address, district, city, subtotal, shipping_fee, total_amount, order_status, payment_status)
VALUES
('ORD001', 4, 1, 1, N'Phạm Khách Hàng', '0909000004', N'123 Lê Văn Việt', N'Thủ Đức', N'TP.HCM', 99000, 25000, 124000, 'Delivered', 'Paid'),
('ORD002', 5, 2, NULL, N'Nguyễn Người Dùng', '0909000005', N'456 Nguyễn Huệ', N'Quận 1', N'TP.HCM', 150000, 20000, 170000, 'Pending', 'Unpaid');

-- =====================================================
-- SEED ORDER ITEMS
-- =====================================================
INSERT INTO Order_Items (order_id, product_id, variant_id, product_name, product_sku, size, color, quantity, unit_price, total_price)
VALUES
(1, 1, 1, N'Áo Thun UTE Classic', 'SKU001-M-ĐEN', 'M', N'Đen', 1, 99000, 99000),
(2, 3, 3, N'Mũ Lưỡi Trai Đen', 'SKU003-ĐEN', NULL, N'Đen', 1, 150000, 150000);

-- =====================================================
-- SEED REVIEWS
-- =====================================================
INSERT INTO Reviews (product_id, user_id, order_id, rating, title, comment, is_verified_purchase, is_approved)
VALUES
(1, 4, 1, 5, N'Áo đẹp và thoải mái', N'Chất vải mềm, logo UTE in đẹp.', 1, 1),
(3, 5, 2, 4, N'Mũ đẹp, giao nhanh', N'Đội vừa vặn, giá hợp lý.', 1, 1);

-- =====================================================
-- SEED WISHLISTS & VIEWED_PRODUCTS
-- =====================================================
INSERT INTO Wishlists (user_id, product_id) VALUES (4, 2), (5, 1);

INSERT INTO Viewed_Products (user_id, product_id) VALUES (4, 1), (4, 3), (5, 2);

-- =====================================================
-- SEED BANNERS, SETTINGS, CONTACTS
-- =====================================================
INSERT INTO Payment_Methods (
    method_name, 
    method_code, 
    description, 
    is_active, 
    display_order
) VALUES (
    N'Thanh toán khi nhận hàng (COD)',
    'COD',
    N'Thanh toán bằng tiền mặt khi nhận hàng. Bạn sẽ trả tiền trực tiếp cho nhân viên giao hàng.',
    1,
    1
);
INSERT INTO Payment_Methods (
    method_name, 
    method_code, 
    description, 
    is_active, 
    display_order
) VALUES (
    N'Chuyển khoản QR Code',
    'SEPAY_QR',
    N'Quét mã QR để thanh toán qua ngân hàng BIDV. Đơn hàng sẽ được tự động xác nhận trong vòng 60 giây.',
    1,
    2
);

INSERT INTO Banners (title, image_url, link_url, description, position, is_active)
VALUES
(N'Giảm giá mùa thu', 'https://example.com/banner1.jpg', '/collections/ao', N'Ưu đãi đến 50% cho áo thun', 'home_top', 1),
(N'Sản phẩm mới về', 'https://example.com/banner2.jpg', '/new-arrivals', N'Hàng mới từ Yame', 'home_mid', 1);

INSERT INTO Settings (setting_key, setting_value, setting_type, description)
VALUES
('site_name', N'UTE Fashion', 'text', N'Tên website'),
('support_email', N'support@ute.vn', 'text', N'Email hỗ trợ khách hàng'),
('default_shipping_fee', N'25000', 'number', N'Phí giao hàng mặc định');

INSERT INTO Contacts (full_name, email, subject, message)
VALUES
(N'Khách hàng A', 'a@example.com', N'Hỏi về đơn hàng', N'Khi nào đơn hàng của tôi được giao?');

PRINT 'Dữ liệu mẫu UTE_Fashion đã được thêm thành công!';
GO


