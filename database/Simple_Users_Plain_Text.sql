-- =============================================
-- Script: Simple Users với Plain Text Password
-- Mô tả: Tạo users đơn giản với password plain text để test
-- =============================================

USE UTE_Fashion;
GO

-- =============================================
-- BƯỚC 1: Xóa dữ liệu cũ
-- =============================================

DELETE FROM User_Roles;
DELETE FROM Users WHERE username IN ('admin', 'user1', 'user2');
DELETE FROM Roles;
GO

-- =============================================
-- BƯỚC 2: Tạo Roles
-- =============================================

INSERT INTO Roles (role_name, description, created_at, updated_at) VALUES
('ADMIN', 'Administrator - Full access', GETDATE(), GETDATE()),
('USER', 'Regular User - Customer', GETDATE(), GETDATE()),
('MANAGER', 'Manager - Manage products and orders', GETDATE(), GETDATE());
GO

-- =============================================
-- BƯỚC 3: Tạo Users với Plain Text Password
-- =============================================

-- Admin Account
INSERT INTO Users (
    username,
    email,
    password_hash,
    full_name,
    phone_number,
    is_active,
    is_email_verified,
    created_at,
    updated_at
) VALUES (
    'admin',
    'admin@utefashion.com',
    'admin123',  -- Plain text password
    'Administrator',
    '0123456789',
    1,
    1,
    GETDATE(),
    GETDATE()
);
GO

-- User Test 1
INSERT INTO Users (
    username,
    email,
    password_hash,
    full_name,
    phone_number,
    is_active,
    is_email_verified,
    created_at,
    updated_at
) VALUES (
    'user1',
    'user1@example.com',
    'user123',  -- Plain text password
    'User Test 1',
    '0987654321',
    1,
    1,
    GETDATE(),
    GETDATE()
);
GO

-- User Test 2
INSERT INTO Users (
    username,
    email,
    password_hash,
    full_name,
    phone_number,
    is_active,
    is_email_verified,
    created_at,
    updated_at
) VALUES (
    'user2',
    'user2@example.com',
    'user123',  -- Plain text password
    'User Test 2',
    '0912345678',
    1,
    0,
    GETDATE(),
    GETDATE()
);
GO

-- =============================================
-- BƯỚC 4: Gán Roles cho Users
-- =============================================

-- Gán role ADMIN cho user admin
INSERT INTO User_Roles (user_id, role_id, assigned_at)
SELECT u.user_id, r.role_id, GETDATE()
FROM Users u, Roles r
WHERE u.username = 'admin' AND r.role_name = 'ADMIN';
GO

-- Gán role USER cho user1 và user2
INSERT INTO User_Roles (user_id, role_id, assigned_at)
SELECT u.user_id, r.role_id, GETDATE()
FROM Users u, Roles r
WHERE u.username IN ('user1', 'user2') AND r.role_name = 'USER';
GO

-- =============================================
-- BƯỚC 5: Kiểm tra kết quả
-- =============================================

-- Kiểm tra Users
SELECT 
    user_id,
    username,
    email,
    password_hash,
    full_name,
    phone_number,
    is_active,
    is_email_verified,
    created_at
FROM Users
WHERE username IN ('admin', 'user1', 'user2')
ORDER BY user_id;
GO

-- Kiểm tra Roles
SELECT * FROM Roles ORDER BY role_id;
GO

-- Kiểm tra User_Roles
SELECT 
    u.username,
    r.role_name,
    ur.assigned_at
FROM User_Roles ur
INNER JOIN Users u ON ur.user_id = u.user_id
INNER JOIN Roles r ON ur.role_id = r.role_id
ORDER BY u.username, r.role_name;
GO

PRINT 'Đã tạo thành công các tài khoản test với plain text password!';
PRINT '';
PRINT '=== THÔNG TIN TÀI KHOẢN ===';
PRINT 'Admin Account:';
PRINT '  Username: admin';
PRINT '  Password: admin123';
PRINT '  Email: admin@utefashion.com';
PRINT '  Role: ADMIN';
PRINT '';
PRINT 'User Test 1:';
PRINT '  Username: user1';
PRINT '  Password: user123';
PRINT '  Email: user1@example.com';
PRINT '  Role: USER';
PRINT '';
PRINT 'User Test 2:';
PRINT '  Username: user2';
PRINT '  Password: user123';
PRINT '  Email: user2@example.com';
PRINT '  Role: USER';
PRINT '';
PRINT '=== LƯU Ý ===';
PRINT 'Tất cả password đều là plain text (không hash)';
PRINT 'Chỉ dùng cho testing, không dùng trong production!';
GO
