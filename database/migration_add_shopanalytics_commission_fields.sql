-- Migration: Add Commission Fields to ShopAnalytics table
-- Date: 2025-10-24
-- Description: Thêm các cột commission_percentage, commission_amount, shop_net_revenue vào bảng ShopAnalytics

USE UTE_Fashion;
GO

-- Check and add commission_percentage if not exists
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'ShopAnalytics' AND COLUMN_NAME = 'commission_percentage'
)
BEGIN
    ALTER TABLE ShopAnalytics
    ADD commission_percentage DECIMAL(5, 2) DEFAULT 0.00 NOT NULL;
    
    PRINT '✅ Added commission_percentage column to ShopAnalytics table'
END
ELSE
BEGIN
    PRINT '⚠️  commission_percentage column already exists in ShopAnalytics table'
END
GO

-- Check and add commission_amount if not exists
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'ShopAnalytics' AND COLUMN_NAME = 'commission_amount'
)
BEGIN
    ALTER TABLE ShopAnalytics
    ADD commission_amount DECIMAL(18, 2) DEFAULT 0.00 NOT NULL;
    
    PRINT '✅ Added commission_amount column to ShopAnalytics table'
END
ELSE
BEGIN
    PRINT '⚠️  commission_amount column already exists in ShopAnalytics table'
END
GO

-- Check and add shop_net_revenue if not exists
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'ShopAnalytics' AND COLUMN_NAME = 'shop_net_revenue'
)
BEGIN
    ALTER TABLE ShopAnalytics
    ADD shop_net_revenue DECIMAL(18, 2) DEFAULT 0.00 NOT NULL;
    
    PRINT '✅ Added shop_net_revenue column to ShopAnalytics table'
END
ELSE
BEGIN
    PRINT '⚠️  shop_net_revenue column already exists in ShopAnalytics table'
END
GO

-- Create indexes if they don't exist
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'idx_shopanalytics_commission_amount' AND object_id = OBJECT_ID('ShopAnalytics')
)
BEGIN
    CREATE INDEX idx_shopanalytics_commission_amount ON ShopAnalytics(commission_amount);
    PRINT '✅ Created index idx_shopanalytics_commission_amount'
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'idx_shopanalytics_shop_net_revenue' AND object_id = OBJECT_ID('ShopAnalytics')
)
BEGIN
    CREATE INDEX idx_shopanalytics_shop_net_revenue ON ShopAnalytics(shop_net_revenue);
    PRINT '✅ Created index idx_shopanalytics_shop_net_revenue'
END
GO

PRINT '✅ Migration: ShopAnalytics commission fields completed'
GO
