-- =============================================
-- INSERT USERS DATA - SCRIPT CHỈ INSERT VÀO BẢNG USERS
-- =============================================
-- Tạo các user mặc định: Admin, User test, Vendor, Shipper
-- Tất cả password đều là: 123456 (BCrypt hash)
-- Script này NOT xóa dữ liệu cũ, chỉ insert thêm

USE UTE_Fashion;
GO

PRINT '=========================================='
PRINT 'INSERT DỮ LIỆU VÀO BẢNG USERS'
PRINT '=========================================='
PRINT ''

-- =============================================
-- BƯỚC 1: Kiểm tra Roles có tồn tại không
-- =============================================
PRINT '✓ BƯỚC 1: Kiểm tra Roles tồn tại...'
IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'ADMIN')
BEGIN
    INSERT INTO Roles (role_name, description, created_at, updated_at) 
    VALUES ('ADMIN', 'Administrator - Full access', GETDATE(), GETDATE());
    PRINT '  → Tạo role ADMIN'
END
ELSE
    PRINT '  → Role ADMIN đã tồn tại'

IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'USER')
BEGIN
    INSERT INTO Roles (role_name, description, created_at, updated_at) 
    VALUES ('USER', 'Regular User - Customer', GETDATE(), GETDATE());
    PRINT '  → Tạo role USER'
END
ELSE
    PRINT '  → Role USER đã tồn tại'

IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'VENDOR')
BEGIN
    INSERT INTO Roles (role_name, description, created_at, updated_at) 
    VALUES ('VENDOR', 'Vendor - Shop owner', GETDATE(), GETDATE());
    PRINT '  → Tạo role VENDOR'
END
ELSE
    PRINT '  → Role VENDOR đã tồn tại'

IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'SHIPPER')
BEGIN
    INSERT INTO Roles (role_name, description, created_at, updated_at) 
    VALUES ('SHIPPER', 'Shipper - Delivery staff', GETDATE(), GETDATE());
    PRINT '  → Tạo role SHIPPER'
END
ELSE
    PRINT '  → Role SHIPPER đã tồn tại'

GO

PRINT ''

-- =============================================
-- BƯỚC 2: Insert Users (nếu chưa tồn tại)
-- =============================================
PRINT '✓ BƯỚC 2: Insert Users mặc định...'
PRINT ''

-- 1. Admin User
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'admin')
BEGIN
    INSERT INTO Users (
        username, email, password_hash, full_name, phone_number,
        is_active, is_email_verified, created_at, updated_at
    ) VALUES (
        'admin',
        'admin@utefashion.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: admin123
        'Administrator',
        '0123456789',
        1, 1, GETDATE(), GETDATE()
    );
    PRINT '  ✅ Inserted: admin (admin@utefashion.com) - Pass: admin123'
END
ELSE
    PRINT '  ⚠️  admin đã tồn tại - SKIP'

-- 2. Regular User 1
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'user1')
BEGIN
    INSERT INTO Users (
        username, email, password_hash, full_name, phone_number,
        is_active, is_email_verified, created_at, updated_at
    ) VALUES (
        'user1',
        'user1@utefashion.com',
        '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5g8Jnp/bKLmGe', -- Password: 123456
        'User Test 1',
        '0987654321',
        1, 1, GETDATE(), GETDATE()
    );
    PRINT '  ✅ Inserted: user1 (user1@utefashion.com) - Pass: 123456'
END
ELSE
    PRINT '  ⚠️  user1 đã tồn tại - SKIP'

-- 3. Regular User 2
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'user2')
BEGIN
    INSERT INTO Users (
        username, email, password_hash, full_name, phone_number,
        is_active, is_email_verified, created_at, updated_at
    ) VALUES (
        'user2',
        'user2@utefashion.com',
        '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5g8Jnp/bKLmGe', -- Password: 123456
        'User Test 2',
        '0912345678',
        1, 0, GETDATE(), GETDATE()
    );
    PRINT '  ✅ Inserted: user2 (user2@utefashion.com) - Pass: 123456'
END
ELSE
    PRINT '  ⚠️  user2 đã tồn tại - SKIP'

-- 4. Vendor User
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'vendor1')
BEGIN
    INSERT INTO Users (
        username, email, password_hash, full_name, phone_number,
        is_active, is_email_verified, created_at, updated_at
    ) VALUES (
        'vendor1',
        'vendor1@utefashion.com',
        '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5g8Jnp/bKLmGe', -- Password: 123456
        'Vendor Test 1',
        '0911223344',
        1, 1, GETDATE(), GETDATE()
    );
    PRINT '  ✅ Inserted: vendor1 (vendor1@utefashion.com) - Pass: 123456'
END
ELSE
    PRINT '  ⚠️  vendor1 đã tồn tại - SKIP'

-- 5. Shipper User
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'shipper1')
BEGIN
    INSERT INTO Users (
        username, email, password_hash, full_name, phone_number,
        is_active, is_email_verified, created_at, updated_at
    ) VALUES (
        'shipper1',
        'shipper1@utefashion.com',
        '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5g8Jnp/bKLmGe', -- Password: 123456
        'Shipper Test 1',
        '0922334455',
        1, 1, GETDATE(), GETDATE()
    );
    PRINT '  ✅ Inserted: shipper1 (shipper1@utefashion.com) - Pass: 123456'
END
ELSE
    PRINT '  ⚠️  shipper1 đã tồn tại - SKIP'

GO

PRINT ''

-- =============================================
-- BƯỚC 3: Gán Roles cho Users (nếu chưa có)
-- =============================================
PRINT '✓ BƯỚC 3: Gán Roles cho Users...'
PRINT ''

-- Admin role
IF NOT EXISTS (
    SELECT 1 FROM User_Roles ur
    INNER JOIN Users u ON ur.user_id = u.user_id
    INNER JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'admin' AND r.role_name = 'ADMIN'
)
BEGIN
    INSERT INTO User_Roles (user_id, role_id, assigned_at)
    SELECT u.user_id, r.role_id, GETDATE()
    FROM Users u, Roles r
    WHERE u.username = 'admin' AND r.role_name = 'ADMIN';
    PRINT '  ✅ Gán role ADMIN cho admin'
END
ELSE
    PRINT '  ⚠️  admin đã có role ADMIN - SKIP'

-- USER roles cho user1 và user2
IF NOT EXISTS (
    SELECT 1 FROM User_Roles ur
    INNER JOIN Users u ON ur.user_id = u.user_id
    INNER JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'user1' AND r.role_name = 'USER'
)
BEGIN
    INSERT INTO User_Roles (user_id, role_id, assigned_at)
    SELECT u.user_id, r.role_id, GETDATE()
    FROM Users u, Roles r
    WHERE u.username = 'user1' AND r.role_name = 'USER';
    PRINT '  ✅ Gán role USER cho user1'
END
ELSE
    PRINT '  ⚠️  user1 đã có role USER - SKIP'

IF NOT EXISTS (
    SELECT 1 FROM User_Roles ur
    INNER JOIN Users u ON ur.user_id = u.user_id
    INNER JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'user2' AND r.role_name = 'USER'
)
BEGIN
    INSERT INTO User_Roles (user_id, role_id, assigned_at)
    SELECT u.user_id, r.role_id, GETDATE()
    FROM Users u, Roles r
    WHERE u.username = 'user2' AND r.role_name = 'USER';
    PRINT '  ✅ Gán role USER cho user2'
END
ELSE
    PRINT '  ⚠️  user2 đã có role USER - SKIP'

-- VENDOR role
IF NOT EXISTS (
    SELECT 1 FROM User_Roles ur
    INNER JOIN Users u ON ur.user_id = u.user_id
    INNER JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'vendor1' AND r.role_name = 'VENDOR'
)
BEGIN
    INSERT INTO User_Roles (user_id, role_id, assigned_at)
    SELECT u.user_id, r.role_id, GETDATE()
    FROM Users u, Roles r
    WHERE u.username = 'vendor1' AND r.role_name = 'VENDOR';
    PRINT '  ✅ Gán role VENDOR cho vendor1'
END
ELSE
    PRINT '  ⚠️  vendor1 đã có role VENDOR - SKIP'

-- SHIPPER role
IF NOT EXISTS (
    SELECT 1 FROM User_Roles ur
    INNER JOIN Users u ON ur.user_id = u.user_id
    INNER JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'shipper1' AND r.role_name = 'SHIPPER'
)
BEGIN
    INSERT INTO User_Roles (user_id, role_id, assigned_at)
    SELECT u.user_id, r.role_id, GETDATE()
    FROM Users u, Roles r
    WHERE u.username = 'shipper1' AND r.role_name = 'SHIPPER';
    PRINT '  ✅ Gán role SHIPPER cho shipper1'
END
ELSE
    PRINT '  ⚠️  shipper1 đã có role SHIPPER - SKIP'

GO

PRINT ''

-- =============================================
-- BƯỚC 4: Hiển thị kết quả
-- =============================================
PRINT '✓ BƯỚC 4: Kiểm tra kết quả...'
PRINT ''

SELECT 
    u.user_id AS 'ID',
    u.username AS 'Username',
    u.email AS 'Email',
    u.full_name AS 'Họ tên',
    STRING_AGG(r.role_name, ', ') AS 'Roles',
    CASE WHEN u.is_active = 1 THEN '✓ Active' ELSE '✗ Inactive' END AS 'Trạng thái',
    CASE WHEN u.is_email_verified = 1 THEN '✓ Verified' ELSE '✗ Not verified' END AS 'Email'
FROM Users u
LEFT JOIN User_Roles ur ON u.user_id = ur.user_id
LEFT JOIN Roles r ON ur.role_id = r.role_id
GROUP BY u.user_id, u.username, u.email, u.full_name, u.is_active, u.is_email_verified
ORDER BY u.user_id;

GO

PRINT ''
PRINT '=========================================='
PRINT '✅ INSERT DỮ LIỆU XONG!'
PRINT '=========================================='
PRINT ''
PRINT '📋 THÔNG TIN TÀI KHOẢN:'
PRINT '  1. Admin: admin / admin123'
PRINT '  2. User 1: user1 / 123456'
PRINT '  3. User 2: user2 / 123456'
PRINT '  4. Vendor: vendor1 / 123456'
PRINT '  5. Shipper: shipper1 / 123456'
PRINT ''
GO
