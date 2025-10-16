-- =====================================================
-- THÊM DỮ LIỆU VÀO GIỎ HÀNG CHO USER01
-- =====================================================
USE UTE_Fashion;
GO

-- Kiểm tra user01 có user_id nào
DECLARE @user01_id INT;
SELECT @user01_id = user_id FROM Users WHERE username = 'user01';

IF @user01_id IS NULL
BEGIN
    PRINT 'ERROR: Không tìm thấy user01!';
    RETURN;
END

PRINT 'User ID của user01: ' + CAST(@user01_id AS NVARCHAR(10));

-- Xóa cart cũ của user01 (nếu có)
DECLARE @cart_id INT;
SELECT @cart_id = cart_id FROM Carts WHERE user_id = @user01_id;

IF @cart_id IS NOT NULL
BEGIN
    DELETE FROM Cart_Items WHERE cart_id = @cart_id;
    DELETE FROM Carts WHERE cart_id = @cart_id;
    PRINT 'Đã xóa giỏ hàng cũ của user01';
END

-- Tạo giỏ hàng mới cho user01
INSERT INTO Carts (user_id, session_id, created_at, updated_at)
VALUES (@user01_id, NULL, GETDATE(), GETDATE());

SET @cart_id = SCOPE_IDENTITY();
PRINT 'Đã tạo giỏ hàng mới với cart_id: ' + CAST(@cart_id AS NVARCHAR(10));

-- Thêm sản phẩm vào giỏ hàng
-- Sản phẩm 1: Áo Thun UTE Classic (M, Đen) - Số lượng 2
INSERT INTO Cart_Items (cart_id, product_id, variant_id, quantity, price, created_at, updated_at)
VALUES (
    @cart_id,
    1, -- product_id: Áo Thun UTE Classic
    1, -- variant_id: Size M, Màu Đen
    2, -- Số lượng: 2
    99000, -- Giá sale
    GETDATE(),
    GETDATE()
);
PRINT 'Đã thêm: Áo Thun UTE Classic (M, Đen) x 2';

-- Sản phẩm 2: Quần Jean Xanh (L, Xanh) - Số lượng 1
INSERT INTO Cart_Items (cart_id, product_id, variant_id, quantity, price, created_at, updated_at)
VALUES (
    @cart_id,
    2, -- product_id: Quần Jean Xanh
    2, -- variant_id: Size L, Màu Xanh
    1, -- Số lượng: 1
    350000, -- Giá gốc
    GETDATE(),
    GETDATE()
);
PRINT 'Đã thêm: Quần Jean Xanh (L, Xanh) x 1';

-- Sản phẩm 3: Mũ Lưỡi Trai Đen - Số lượng 1
INSERT INTO Cart_Items (cart_id, product_id, variant_id, quantity, price, created_at, updated_at)
VALUES (
    @cart_id,
    3, -- product_id: Mũ Lưỡi Trai Đen
    3, -- variant_id: Đen
    1, -- Số lượng: 1
    125000, -- Giá sale
    GETDATE(),
    GETDATE()
);
PRINT 'Đã thêm: Mũ Lưỡi Trai Đen x 1';

-- Cập nhật updated_at của cart
UPDATE Carts SET updated_at = GETDATE() WHERE cart_id = @cart_id;

-- Hiển thị kết quả
PRINT '';
PRINT '===== THỐNG KÊ GIỎ HÀNG CỦA USER01 =====';

SELECT 
    c.cart_id,
    u.username,
    u.full_name,
    COUNT(ci.cart_item_id) AS total_items,
    SUM(ci.quantity) AS total_quantity,
    SUM(ci.price * ci.quantity) AS total_amount
FROM Carts c
JOIN Users u ON c.user_id = u.user_id
LEFT JOIN Cart_Items ci ON c.cart_id = ci.cart_id
WHERE c.cart_id = @cart_id
GROUP BY c.cart_id, u.username, u.full_name;

PRINT '';
PRINT '===== CHI TIẾT SẢN PHẨM TRONG GIỎ =====';

SELECT 
    ci.cart_item_id,
    p.product_name,
    s.size_name AS size,
    col.color_name AS color,
    ci.quantity,
    ci.price,
    (ci.quantity * ci.price) AS subtotal
FROM Cart_Items ci
JOIN Products p ON ci.product_id = p.product_id
LEFT JOIN Product_Variants pv ON ci.variant_id = pv.variant_id
LEFT JOIN Sizes s ON pv.size_id = s.size_id
LEFT JOIN Colors col ON pv.color_id = col.color_id
WHERE ci.cart_id = @cart_id;

PRINT '';
PRINT '✅ HOÀN THÀNH! Giỏ hàng của user01 đã được tạo thành công!';
PRINT 'Tổng cộng: 3 sản phẩm (4 items)';
PRINT 'Tạm tính: 198,000 + 350,000 + 125,000 = 673,000 VNĐ';
GO
