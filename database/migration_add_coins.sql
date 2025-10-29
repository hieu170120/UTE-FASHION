-- =============================================
-- MIGRATION: Thêm trường coins vào bảng Users
-- Mô tả: Thêm hệ thống xu cho user để thanh toán
-- =============================================

USE UTE_Fashion;
GO

-- Thêm cột coins vào bảng Users
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'Users') 
    AND name = 'coins'
)
BEGIN
    ALTER TABLE Users
    ADD coins DECIMAL(18,2) DEFAULT 0 NOT NULL;
    
    PRINT 'Added column coins to Users table';
END
ELSE
BEGIN
    PRINT 'Column coins already exists in Users table';
END
GO

-- Cập nhật coins = 0 cho tất cả user hiện tại (nếu NULL)
UPDATE Users
SET coins = 0
WHERE coins IS NULL;
GO

PRINT 'Migration completed: Added coins system to Users';
GO

