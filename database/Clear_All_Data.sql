-- =============================================
-- Script: Xóa dữ liệu cũ trong database
-- Mô tả: Xóa tất cả dữ liệu users, roles và relationships
-- =============================================

USE UTE_Fashion;
GO

-- =============================================
-- BƯỚC 1: Xóa User_Roles (bảng liên kết)
-- =============================================

PRINT 'Đang xóa User_Roles...';
DELETE FROM User_Roles;
PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ User_Roles';
GO

-- =============================================
-- BƯỚC 2: Xóa Users
-- =============================================

PRINT 'Đang xóa Users...';
DELETE FROM Users;
PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ Users';
GO

-- =============================================
-- BƯỚC 3: Xóa Roles
-- =============================================

PRINT 'Đang xóa Roles...';
DELETE FROM Roles;
PRINT 'Đã xóa ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' records từ Roles';
GO

-- =============================================
-- BƯỚC 4: Reset Identity (tùy chọn)
-- =============================================

-- Reset identity cho Users table
DBCC CHECKIDENT ('Users', RESEED, 0);
PRINT 'Đã reset identity cho Users table';

-- Reset identity cho Roles table  
DBCC CHECKIDENT ('Roles', RESEED, 0);
PRINT 'Đã reset identity cho Roles table';
GO

-- =============================================
-- BƯỚC 5: Kiểm tra kết quả
-- =============================================

PRINT '';
PRINT '=== KIỂM TRA KẾT QUẢ ===';

-- Kiểm tra Users
SELECT COUNT(*) AS 'Số lượng Users' FROM Users;
GO

-- Kiểm tra Roles
SELECT COUNT(*) AS 'Số lượng Roles' FROM Roles;
GO

-- Kiểm tra User_Roles
SELECT COUNT(*) AS 'Số lượng User_Roles' FROM User_Roles;
GO

PRINT '';
PRINT '=== HOÀN THÀNH ===';
PRINT 'Đã xóa tất cả dữ liệu users, roles và relationships';
PRINT 'Database đã được làm sạch và sẵn sàng cho dữ liệu mới';
GO
