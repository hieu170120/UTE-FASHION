-- =============================================
-- Script: Xóa Users cụ thể
-- Mô tả: Chỉ xóa các users test cụ thể, giữ lại dữ liệu khác
-- =============================================

USE UTE_Fashion;
GO

-- =============================================
-- BƯỚC 1: Xóa User_Roles của users test
-- =============================================

PRINT 'Đang xóa User_Roles của users test...';

DELETE ur 
FROM User_Roles ur
INNER JOIN Users u ON ur.user_id = u.user_id
WHERE u.username IN ('admin', 'user1', 'user2', 'test', 'testuser');

PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ User_Roles';
GO

-- =============================================
-- BƯỚC 2: Xóa Users test
-- =============================================

PRINT 'Đang xóa Users test...';

DELETE FROM Users 
WHERE username IN ('admin', 'user1', 'user2', 'test', 'testuser');

PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ Users';
GO

-- =============================================
-- BƯỚC 3: Xóa Roles test (nếu không có users nào khác sử dụng)
-- =============================================

PRINT 'Đang kiểm tra và xóa Roles test...';

-- Chỉ xóa roles nếu không có users nào khác sử dụng
DELETE FROM Roles 
WHERE role_name IN ('ADMIN', 'USER', 'MANAGER', 'TEST')
  AND role_id NOT IN (
    SELECT DISTINCT ur.role_id 
    FROM User_Roles ur
  );

PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ Roles';
GO

-- =============================================
-- BƯỚC 4: Kiểm tra kết quả
-- =============================================

PRINT '';
PRINT '=== KIỂM TRA KẾT QUẢ ===';

-- Kiểm tra Users còn lại
SELECT 
    user_id,
    username,
    email,
    full_name,
    is_active,
    created_at
FROM Users
ORDER BY user_id;
GO

-- Kiểm tra Roles còn lại
SELECT 
    role_id,
    role_name,
    description,
    created_at
FROM Roles
ORDER BY role_id;
GO

-- Kiểm tra User_Roles còn lại
SELECT 
    u.username,
    r.role_name,
    ur.assigned_at
FROM User_Roles ur
INNER JOIN Users u ON ur.user_id = u.user_id
INNER JOIN Roles r ON ur.role_id = r.role_id
ORDER BY u.username, r.role_name;
GO

PRINT '';
PRINT '=== HOÀN THÀNH ===';
PRINT 'Đã xóa các users test cụ thể';
PRINT 'Dữ liệu khác đã được giữ nguyên';
GO


