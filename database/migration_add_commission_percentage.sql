-- Migration: Add Commission Percentage to Shops table
-- Date: 2025-10-24
-- Author: Admin
-- Description: Thêm cột commission_percentage để quản lý chiết khấu cho mỗi shop

USE UTE_Fashion;
GO

-- Check if column already exists
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Shops' AND COLUMN_NAME = 'commission_percentage'
)
BEGIN
    ALTER TABLE Shops
    ADD commission_percentage DECIMAL(5, 2) DEFAULT 0.00 NOT NULL;
    
    PRINT '✅ Added commission_percentage column to Shops table'
END
ELSE
BEGIN
    PRINT '⚠️  commission_percentage column already exists in Shops table'
END
GO

-- Check if index exists
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'idx_shops_commission_percentage' AND object_id = OBJECT_ID('Shops')
)
BEGIN
    CREATE INDEX idx_shops_commission_percentage ON Shops(commission_percentage);
    PRINT '✅ Created index idx_shops_commission_percentage'
END
ELSE
BEGIN
    PRINT '⚠️  Index idx_shops_commission_percentage already exists'
END
GO

PRINT '✅ Migration: Shops.commission_percentage completed'
GO
