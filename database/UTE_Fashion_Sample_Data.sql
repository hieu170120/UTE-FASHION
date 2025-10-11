-- =============================================
-- UTE FASHION - SAMPLE DATA
-- Dữ liệu mẫu để test
-- =============================================

USE UTE_Fashion;
GO

-- =============================================
-- INSERT ROLES
-- =============================================

INSERT INTO Roles (role_name, description) VALUES
('ADMIN', N'Quản trị viên hệ thống'),
('MANAGER', N'Quản lý cửa hàng'),
('USER', N'Khách hàng'),
('STAFF', N'Nhân viên');

-- =============================================
-- INSERT USERS
-- =============================================

-- Password: "123456" -> BCrypt hash
INSERT INTO Users (username, email, password_hash, full_name, phone_number, gender, is_active, is_email_verified) VALUES
('admin', 'admin@utefashion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Admin UTE Fashion', '0901234567', 'Male', 1, 1),
('manager1', 'manager@utefashion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Văn Quản Lý', '0902234567', 'Male', 1, 1),
('user1', 'user1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Thị Lan Anh', '0903234567', 'Female', 1, 1),
('user2', 'user2@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Văn Minh', '0904234567', 'Male', 1, 1),
('user3', 'user3@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Thị Thu', '0905234567', 'Female', 1, 1);

-- =============================================
-- INSERT USER_ROLES
-- =============================================

INSERT INTO User_Roles (user_id, role_id) VALUES
(1, 1), -- admin has ADMIN role
(2, 2), -- manager1 has MANAGER role
(3, 3), -- user1 has USER role
(4, 3), -- user2 has USER role
(5, 3); -- user3 has USER role

-- =============================================
-- INSERT ADDRESSES
-- =============================================

INSERT INTO Addresses (user_id, recipient_name, phone_number, address_line1, ward, district, city, is_default) VALUES
(3, N'Trần Thị Lan Anh', '0903234567', N'123 Lê Văn Việt', N'Phường Tăng Nhơn Phú A', N'Quận 9', N'TP. Hồ Chí Minh', 1),
(4, N'Lê Văn Minh', '0904234567', N'456 Võ Văn Ngân', N'Phường Linh Chiểu', N'Thủ Đức', N'TP. Hồ Chí Minh', 1),
(5, N'Phạm Thị Thu', '0905234567', N'789 Nguyễn Duy Trinh', N'Phường Bình Trưng Đông', N'Quận 2', N'TP. Hồ Chí Minh', 1);

-- =============================================
-- INSERT CATEGORIES
-- =============================================

-- Parent Categories
INSERT INTO Categories (category_name, slug, description, parent_category_id, display_order, is_active) VALUES
(N'Nam', 'nam', N'Thời trang nam', NULL, 1, 1),
(N'Nữ', 'nu', N'Thời trang nữ', NULL, 2, 1),
(N'Trẻ em', 'tre-em', N'Thời trang trẻ em', NULL, 3, 1),
(N'Phụ kiện', 'phu-kien', N'Phụ kiện thời trang', NULL, 4, 1);

-- Sub Categories - Nam
INSERT INTO Categories (category_name, slug, description, parent_category_id, display_order, is_active) VALUES
(N'Áo thun nam', 'ao-thun-nam', N'Áo thun nam', 1, 1, 1),
(N'Áo sơ mi nam', 'ao-so-mi-nam', N'Áo sơ mi nam', 1, 2, 1),
(N'Quần jean nam', 'quan-jean-nam', N'Quần jean nam', 1, 3, 1),
(N'Quần tây nam', 'quan-tay-nam', N'Quần tây nam', 1, 4, 1),
(N'Áo khoác nam', 'ao-khoac-nam', N'Áo khoác nam', 1, 5, 1);

-- Sub Categories - Nữ
INSERT INTO Categories (category_name, slug, description, parent_category_id, display_order, is_active) VALUES
(N'Áo thun nữ', 'ao-thun-nu', N'Áo thun nữ', 2, 1, 1),
(N'Váy', 'vay', N'Váy nữ', 2, 2, 1),
(N'Quần jean nữ', 'quan-jean-nu', N'Quần jean nữ', 2, 3, 1),
(N'Áo sơ mi nữ', 'ao-so-mi-nu', N'Áo sơ mi nữ', 2, 4, 1),
(N'Đầm', 'dam', N'Đầm nữ', 2, 5, 1);

-- Sub Categories - Phụ kiện
INSERT INTO Categories (category_name, slug, description, parent_category_id, display_order, is_active) VALUES
(N'Túi xách', 'tui-xach', N'Túi xách thời trang', 4, 1, 1),
(N'Giày dép', 'giay-dep', N'Giày dép', 4, 2, 1),
(N'Mũ nón', 'mu-non', N'Mũ nón', 4, 3, 1),
(N'Thắt lưng', 'that-lung', N'Thắt lưng', 4, 4, 1);

-- =============================================
-- INSERT BRANDS
-- =============================================

INSERT INTO Brands (brand_name, slug, description, is_active) VALUES
(N'UTE Fashion', 'ute-fashion', N'Thương hiệu riêng của UTE Fashion', 1),
(N'Nike', 'nike', N'Just Do It', 1),
(N'Adidas', 'adidas', N'Impossible is Nothing', 1),
(N'Uniqlo', 'uniqlo', N'LifeWear', 1),
(N'Zara', 'zara', N'Thời trang Tây Ban Nha', 1),
(N'H&M', 'h-m', N'Thời trang Thụy Điển', 1),
(N'Levi''s', 'levis', N'Thương hiệu jean huyền thoại', 1);

-- =============================================
-- INSERT SIZES
-- =============================================

INSERT INTO Sizes (size_name, size_type, display_order) VALUES
('XS', 'Clothing', 1),
('S', 'Clothing', 2),
('M', 'Clothing', 3),
('L', 'Clothing', 4),
('XL', 'Clothing', 5),
('XXL', 'Clothing', 6),
('38', 'Shoes', 7),
('39', 'Shoes', 8),
('40', 'Shoes', 9),
('41', 'Shoes', 10),
('42', 'Shoes', 11),
('43', 'Shoes', 12);

-- =============================================
-- INSERT COLORS
-- =============================================

INSERT INTO Colors (color_name, color_code) VALUES
(N'Đen', '#000000'),
(N'Trắng', '#FFFFFF'),
(N'Xám', '#808080'),
(N'Đỏ', '#FF0000'),
(N'Xanh dương', '#0000FF'),
(N'Xanh lá', '#00FF00'),
(N'Vàng', '#FFFF00'),
(N'Hồng', '#FFC0CB'),
(N'Nâu', '#A52A2A'),
(N'Be', '#F5F5DC');

-- =============================================
-- INSERT PRODUCTS
-- =============================================

-- Áo thun nam
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_new_arrival, is_active) VALUES
(N'Áo Thun Nam Basic Trắng', 'ao-thun-nam-basic-trang', 'ATN001', 5, 1, N'Áo thun nam basic chất liệu cotton 100%, thoáng mát, thấm hút mồ hôi tốt. Thiết kế đơn giản, dễ phối đồ.', N'Áo thun nam basic cotton 100%', 199000, 149000, 100, 1, 1, 1),
(N'Áo Thun Nam Polo Xanh Navy', 'ao-thun-nam-polo-xanh-navy', 'ATN002', 5, 4, N'Áo polo nam cao cấp, thiết kế thanh lịch phù hợp đi làm và dạo phố.', N'Áo polo nam thanh lịch', 299000, 249000, 80, 1, 1, 1),
(N'Áo Thun Nam In Họa Tiết', 'ao-thun-nam-in-hoa-tiet', 'ATN003', 5, 2, N'Áo thun nam in họa tiết độc đáo, phong cách năng động trẻ trung.', N'Áo thun nam in họa tiết', 259000, NULL, 60, 0, 1, 1);

-- Áo sơ mi nam
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_active) VALUES
(N'Áo Sơ Mi Nam Trắng Oxford', 'ao-so-mi-nam-trang-oxford', 'ASM001', 6, 1, N'Áo sơ mi nam chất liệu Oxford cao cấp, form dáng slim fit thanh lịch. Phù hợp đi làm và dự tiệc.', N'Áo sơ mi Oxford cao cấp', 399000, 349000, 50, 1, 1),
(N'Áo Sơ Mi Nam Kẻ Sọc', 'ao-so-mi-nam-ke-soc', 'ASM002', 6, 4, N'Áo sơ mi nam kẻ sọc phong cách Hàn Quốc, trẻ trung và lịch sự.', N'Áo sơ mi kẻ sọc', 349000, NULL, 40, 0, 1);

-- Quần jean nam
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_best_seller, is_active) VALUES
(N'Quần Jean Nam Slim Fit Đen', 'quan-jean-nam-slim-fit-den', 'QJN001', 7, 7, N'Quần jean nam slim fit chất liệu denim cao cấp, co giãn tốt, tôn dáng. Màu đen trơn dễ phối đồ.', N'Quần jean nam slim fit', 499000, 449000, 70, 1, 1, 1),
(N'Quần Jean Nam Xanh Nhạt Rách', 'quan-jean-nam-xanh-nhat-rach', 'QJN002', 7, 7, N'Quần jean nam rách phong cách streetwear, năng động cá tính.', N'Quần jean nam rách', 549000, NULL, 55, 0, 1, 1);

-- Áo thun nữ
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_new_arrival, is_active) VALUES
(N'Áo Thun Nữ Baby Tee Trắng', 'ao-thun-nu-baby-tee-trang', 'ATNu001', 10, 1, N'Áo thun nữ baby tee form ngắn, chất liệu cotton mềm mại. Phong cách trẻ trung, năng động.', N'Áo thun nữ baby tee', 179000, 139000, 120, 1, 1, 1),
(N'Áo Thun Nữ Croptop Hồng', 'ao-thun-nu-croptop-hong', 'ATNu002', 10, 5, N'Áo croptop nữ màu hồng pastel dễ thương, phù hợp mùa hè.', N'Áo croptop nữ', 199000, 169000, 90, 1, 1, 1);

-- Váy
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_best_seller, is_active) VALUES
(N'Váy Xòe Hoa Nhí', 'vay-xoe-hoa-nhi', 'VAY001', 11, 5, N'Váy xòe họa tiết hoa nhí xinh xắn, phong cách vintage. Chất liệu vải mềm mại, thoáng mát.', N'Váy xòe hoa nhí vintage', 349000, 299000, 45, 1, 1, 1),
(N'Váy Midi Đen Thanh Lịch', 'vay-midi-den-thanh-lich', 'VAY002', 11, 6, N'Váy midi đen sang trọng, thiết kế thanh lịch phù hợp đi làm.', N'Váy midi đen sang trọng', 449000, NULL, 35, 1, 0, 1);

-- Đầm
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_active) VALUES
(N'Đầm Maxi Trắng Dự Tiệc', 'dam-maxi-trang-du-tiec', 'DAM001', 14, 5, N'Đầm maxi trắng sang trọng, thiết kế thanh lịch phù hợp dự tiệc và sự kiện.', N'Đầm maxi trắng sang trọng', 699000, 599000, 25, 1, 1),
(N'Đầm Suông Công Sở', 'dam-suong-cong-so', 'DAM002', 14, 4, N'Đầm suông công sở thoải mái, phong cách tối giản hiện đại.', N'Đầm suông công sở', 449000, NULL, 40, 0, 1);

-- Túi xách
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_active) VALUES
(N'Túi Xách Tote Canvas', 'tui-xach-tote-canvas', 'TUI001', 15, 1, N'Túi tote canvas đơn giản, tiện lợi. Có thể đựng laptop và tài liệu.', N'Túi tote canvas tiện lợi', 199000, 159000, 80, 0, 1),
(N'Túi Đeo Chéo Mini Da', 'tui-deo-cheo-mini-da', 'TUI002', 15, 1, N'Túi đeo chéo mini da PU cao cấp, nhỏ gọn xinh xắn.', N'Túi đeo chéo mini', 299000, 249000, 60, 1, 1);

-- Giày dép
INSERT INTO Products (product_name, slug, sku, category_id, brand_id, description, short_description, price, sale_price, stock_quantity, is_featured, is_best_seller, is_active) VALUES
(N'Giày Sneaker Trắng Basic', 'giay-sneaker-trang-basic', 'GIAY001', 16, 2, N'Giày sneaker trắng basic, phong cách minimalist. Dễ phối đồ, phù hợp mọi hoàn cảnh.', N'Giày sneaker trắng basic', 599000, 549000, 100, 1, 1, 1),
(N'Giày Thể Thao Nam Nike', 'giay-the-thao-nam-nike', 'GIAY002', 16, 2, N'Giày thể thao Nike chính hãng, êm ái thoải mái.', N'Giày Nike chính hãng', 1990000, 1790000, 30, 1, 1, 1);

-- =============================================
-- INSERT PRODUCT_IMAGES
-- =============================================

-- Images cho Áo Thun Nam Basic Trắng
INSERT INTO Product_Images (product_id, image_url, alt_text, display_order, is_primary) VALUES
(1, '/images/products/ao-thun-nam-basic-trang-1.jpg', N'Áo thun nam basic trắng', 1, 1),
(1, '/images/products/ao-thun-nam-basic-trang-2.jpg', N'Chi tiết áo thun nam', 2, 0);

-- Images cho các sản phẩm khác (simplified)
INSERT INTO Product_Images (product_id, image_url, alt_text, display_order, is_primary) VALUES
(2, '/images/products/ao-polo-nam-xanh.jpg', N'Áo polo nam xanh navy', 1, 1),
(3, '/images/products/ao-thun-nam-hoa-tiet.jpg', N'Áo thun nam in họa tiết', 1, 1),
(4, '/images/products/ao-so-mi-trang.jpg', N'Áo sơ mi trắng Oxford', 1, 1),
(5, '/images/products/ao-so-mi-ke-soc.jpg', N'Áo sơ mi kẻ sọc', 1, 1),
(6, '/images/products/quan-jean-den.jpg', N'Quần jean nam đen', 1, 1),
(7, '/images/products/quan-jean-xanh-rach.jpg', N'Quần jean nam xanh rách', 1, 1),
(8, '/images/products/ao-thun-nu-baby-tee.jpg', N'Áo baby tee nữ', 1, 1),
(9, '/images/products/ao-croptop-hong.jpg', N'Áo croptop hồng', 1, 1),
(10, '/images/products/vay-xoe-hoa-nhi.jpg', N'Váy xòe hoa nhí', 1, 1),
(11, '/images/products/vay-midi-den.jpg', N'Váy midi đen', 1, 1),
(12, '/images/products/dam-maxi-trang.jpg', N'Đầm maxi trắng', 1, 1),
(13, '/images/products/dam-suong-cong-so.jpg', N'Đầm suông công sở', 1, 1),
(14, '/images/products/tui-tote-canvas.jpg', N'Túi tote canvas', 1, 1),
(15, '/images/products/tui-deo-cheo.jpg', N'Túi đeo chéo mini', 1, 1),
(16, '/images/products/giay-sneaker-trang.jpg', N'Giày sneaker trắng', 1, 1),
(17, '/images/products/giay-nike.jpg', N'Giày Nike', 1, 1);

-- =============================================
-- INSERT PRODUCT_VARIANTS
-- =============================================

-- Variants cho Áo Thun Nam Basic (product_id = 1)
INSERT INTO Product_Variants (product_id, size_id, color_id, sku, stock_quantity) VALUES
(1, 2, 2, 'ATN001-S-WHITE', 20),  -- S, Trắng
(1, 3, 2, 'ATN001-M-WHITE', 30),  -- M, Trắng
(1, 4, 2, 'ATN001-L-WHITE', 30),  -- L, Trắng
(1, 5, 2, 'ATN001-XL-WHITE', 20), -- XL, Trắng
(1, 2, 1, 'ATN001-S-BLACK', 15),  -- S, Đen
(1, 3, 1, 'ATN001-M-BLACK', 25),  -- M, Đen
(1, 4, 1, 'ATN001-L-BLACK', 25);  -- L, Đen

-- Variants cho Quần Jean Nam (product_id = 6)
INSERT INTO Product_Variants (product_id, size_id, color_id, sku, stock_quantity) VALUES
(6, 3, 1, 'QJN001-M-BLACK', 15),  -- M, Đen
(6, 4, 1, 'QJN001-L-BLACK', 25),  -- L, Đen
(6, 5, 1, 'QJN001-XL-BLACK', 20), -- XL, Đen
(6, 6, 1, 'QJN001-XXL-BLACK', 10); -- XXL, Đen

-- Variants cho Giày Sneaker (product_id = 16)
INSERT INTO Product_Variants (product_id, size_id, color_id, sku, stock_quantity) VALUES
(16, 7, 2, 'GIAY001-38-WHITE', 10),  -- 38, Trắng
(16, 8, 2, 'GIAY001-39-WHITE', 15),  -- 39, Trắng
(16, 9, 2, 'GIAY001-40-WHITE', 20),  -- 40, Trắng
(16, 10, 2, 'GIAY001-41-WHITE', 25), -- 41, Trắng
(16, 11, 2, 'GIAY001-42-WHITE', 20), -- 42, Trắng
(16, 12, 2, 'GIAY001-43-WHITE', 10); -- 43, Trắng

-- =============================================
-- INSERT PAYMENT_METHODS
-- =============================================

INSERT INTO Payment_Methods (method_name, method_code, description, display_order, is_active) VALUES
(N'Thanh toán khi nhận hàng (COD)', 'COD', N'Thanh toán bằng tiền mặt khi nhận hàng', 1, 1),
(N'Chuyển khoản ngân hàng', 'BANK_TRANSFER', N'Chuyển khoản qua ngân hàng', 2, 1),
(N'VNPay', 'VNPAY', N'Thanh toán qua VNPay', 3, 1),
(N'MoMo', 'MOMO', N'Thanh toán qua ví MoMo', 4, 1);

-- =============================================
-- INSERT ORDERS (Sample)
-- =============================================

INSERT INTO Orders (order_number, user_id, recipient_name, phone_number, email, shipping_address, ward, district, city, 
                    subtotal, shipping_fee, discount_amount, total_amount, order_status, payment_status, order_date) VALUES
('ORD-2024-0001', 3, N'Trần Thị Lan Anh', '0903234567', 'user1@gmail.com', 
 N'123 Lê Văn Việt', N'Phường Tăng Nhơn Phú A', N'Quận 9', N'TP. Hồ Chí Minh',
 598000, 30000, 0, 628000, 'Delivered', 'Paid', DATEADD(day, -10, GETDATE())),

('ORD-2024-0002', 4, N'Lê Văn Minh', '0904234567', 'user2@gmail.com',
 N'456 Võ Văn Ngân', N'Phường Linh Chiểu', N'Thủ Đức', N'TP. Hồ Chí Minh',
 897000, 30000, 50000, 877000, 'Shipping', 'Paid', DATEADD(day, -3, GETDATE())),

('ORD-2024-0003', 5, N'Phạm Thị Thu', '0905234567', 'user3@gmail.com',
 N'789 Nguyễn Duy Trinh', N'Phường Bình Trưng Đông', N'Quận 2', N'TP. Hồ Chí Minh',
 449000, 30000, 0, 479000, 'Processing', 'Unpaid', DATEADD(day, -1, GETDATE()));

-- =============================================
-- INSERT ORDER_ITEMS
-- =============================================

-- Order 1 items
INSERT INTO Order_Items (order_id, product_id, variant_id, product_name, product_sku, size, color, quantity, unit_price, total_price) VALUES
(1, 1, 2, N'Áo Thun Nam Basic Trắng', 'ATN001-M-WHITE', 'M', N'Trắng', 2, 149000, 298000),
(1, 8, NULL, N'Áo Thun Nữ Baby Tee Trắng', 'ATNu001', NULL, NULL, 2, 139000, 278000);

-- Order 2 items
INSERT INTO Order_Items (order_id, product_id, variant_id, product_name, product_sku, size, color, quantity, unit_price, total_price) VALUES
(2, 6, 8, N'Quần Jean Nam Slim Fit Đen', 'QJN001-L-BLACK', 'L', N'Đen', 1, 449000, 449000),
(2, 4, NULL, N'Áo Sơ Mi Nam Trắng Oxford', 'ASM001', NULL, NULL, 1, 349000, 349000);

-- Order 3 items
INSERT INTO Order_Items (order_id, product_id, product_name, product_sku, quantity, unit_price, total_price) VALUES
(3, 11, N'Váy Midi Đen Thanh Lịch', 'VAY002', 1, 449000, 449000);

-- =============================================
-- INSERT COUPONS
-- =============================================

INSERT INTO Coupons (coupon_code, description, discount_type, discount_value, min_order_value, max_discount_amount, 
                     usage_limit, valid_from, valid_to, is_active) VALUES
('WELCOME2024', N'Mã giảm giá cho khách hàng mới', 'Percentage', 10, 300000, 100000, 100, '2024-01-01', '2024-12-31', 1),
('SALE50K', N'Giảm 50K cho đơn hàng từ 500K', 'Fixed', 50000, 500000, NULL, 200, '2024-01-01', '2024-12-31', 1),
('FREESHIP', N'Miễn phí vận chuyển', 'Fixed', 30000, 200000, NULL, 500, '2024-01-01', '2024-12-31', 1);

-- =============================================
-- INSERT REVIEWS
-- =============================================

INSERT INTO Reviews (product_id, user_id, order_id, rating, title, comment, is_verified_purchase, is_approved) VALUES
(1, 3, 1, 5, N'Sản phẩm tuyệt vời', N'Chất vải mềm mại, mặc rất thoải mái. Shop giao hàng nhanh. Sẽ ủng hộ tiếp!', 1, 1),
(8, 3, 1, 5, N'Rất đẹp và xinh', N'Áo baby tee xinh lắm, form vừa vặn. Mình rất thích!', 1, 1),
(6, 4, 2, 4, N'Tốt nhưng hơi chật', N'Chất jean đẹp nhưng size L hơi chật so với mong đợi. Nên order lên 1 size.', 1, 1);

-- =============================================
-- INSERT WISHLISTS
-- =============================================

INSERT INTO Wishlists (user_id, product_id) VALUES
(3, 12), -- User 1 thích Đầm Maxi Trắng
(3, 17), -- User 1 thích Giày Nike
(4, 10), -- User 2 thích Váy Xòe
(5, 16); -- User 3 thích Giày Sneaker

-- =============================================
-- INSERT BANNERS
-- =============================================

INSERT INTO Banners (title, image_url, link_url, description, position, display_order, is_active, start_date, end_date) VALUES
(N'Banner Sale Mùa Hè', '/images/banners/summer-sale.jpg', '/products?sale=true', N'Giảm giá đến 50% cho các sản phẩm mùa hè', 'Homepage', 1, 1, '2024-05-01', '2024-08-31'),
(N'Banner Bộ Sưu Tập Mới', '/images/banners/new-collection.jpg', '/products?new=true', N'Bộ sưu tập thời trang Thu Đông 2024', 'Homepage', 2, 1, '2024-09-01', '2024-12-31');

-- =============================================
-- INSERT SETTINGS
-- =============================================

INSERT INTO Settings (setting_key, setting_value, setting_type, description) VALUES
('site_name', 'UTE Fashion', 'String', N'Tên website'),
('site_email', 'support@utefashion.com', 'String', N'Email liên hệ'),
('site_phone', '1900-xxxx', 'String', N'Số điện thoại hỗ trợ'),
('shipping_fee', '30000', 'Number', N'Phí vận chuyển mặc định (VNĐ)'),
('free_shipping_threshold', '500000', 'Number', N'Miễn phí ship cho đơn từ (VNĐ)'),
('low_stock_threshold', '10', 'Number', N'Ngưỡng cảnh báo hàng sắp hết'),
('facebook_url', 'https://facebook.com/utefashion', 'String', N'Link Facebook'),
('instagram_url', 'https://instagram.com/utefashion', 'String', N'Link Instagram');

-- =============================================
-- INSERT NOTIFICATIONS
-- =============================================

INSERT INTO Notifications (user_id, notification_type, title, message, link_url, is_read) VALUES
(3, 'ORDER_STATUS', N'Đơn hàng đã được giao thành công', N'Đơn hàng #ORD-2024-0001 của bạn đã được giao thành công!', '/orders/1', 1),
(4, 'ORDER_STATUS', N'Đơn hàng đang được vận chuyển', N'Đơn hàng #ORD-2024-0002 đang trên đường giao đến bạn!', '/orders/2', 0),
(1, 'NEW_ORDER', N'Có đơn hàng mới', N'Đơn hàng #ORD-2024-0003 vừa được tạo!', '/admin/orders/3', 0);

GO

-- =============================================
-- UPDATE STATISTICS
-- =============================================

-- Update product ratings
UPDATE Products SET average_rating = 5.0, review_count = 2 WHERE product_id = 1;
UPDATE Products SET average_rating = 5.0, review_count = 1 WHERE product_id = 8;
UPDATE Products SET average_rating = 4.0, review_count = 1 WHERE product_id = 6;

-- Update product sold count
UPDATE Products SET sold_count = 2 WHERE product_id = 1;
UPDATE Products SET sold_count = 2 WHERE product_id = 8;
UPDATE Products SET sold_count = 1 WHERE product_id = 6;
UPDATE Products SET sold_count = 1 WHERE product_id = 4;
UPDATE Products SET sold_count = 1 WHERE product_id = 11;

GO

PRINT 'Sample data inserted successfully!';
PRINT 'Default login credentials:';
PRINT 'Admin - Username: admin, Password: 123456';
PRINT 'Manager - Username: manager1, Password: 123456';
PRINT 'User - Username: user1, Password: 123456';
GO

