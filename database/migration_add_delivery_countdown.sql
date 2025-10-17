-- =====================================================
-- MIGRATION: Add Delivery Countdown and Carrier Type
-- Date: 2025-10-16
-- Description: Thêm tính năng đếm ngược giao hàng và phân loại carrier
-- =====================================================

USE UTE_Fashion;
GO

-- 1. Thêm fields vào bảng Orders
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Orders]') AND name = 'delivery_countdown_seconds')
BEGIN
    ALTER TABLE Orders ADD delivery_countdown_seconds INT NULL;
    PRINT 'Added delivery_countdown_seconds to Orders table';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Orders]') AND name = 'delivery_started_at')
BEGIN
    ALTER TABLE Orders ADD delivery_started_at DATETIME2 NULL;
    PRINT 'Added delivery_started_at to Orders table';
END

-- 2. Thêm fields vào bảng Carriers
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Carriers]') AND name = 'delivery_type')
BEGIN
    ALTER TABLE Carriers ADD delivery_type NVARCHAR(20) NULL;
    PRINT 'Added delivery_type to Carriers table';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Carriers]') AND name = 'estimated_delivery_minutes')
BEGIN
    ALTER TABLE Carriers ADD estimated_delivery_minutes INT NULL;
    PRINT 'Added estimated_delivery_minutes to Carriers table';
END

-- 3. Cập nhật dữ liệu mẫu cho Carriers (nếu có)
-- Ví dụ: Set delivery_type cho các carrier hiện có
UPDATE Carriers 
SET delivery_type = 'FAST',
    estimated_delivery_minutes = 30
WHERE carrier_name LIKE N'%Nhanh%' OR carrier_name LIKE N'%Express%';

UPDATE Carriers 
SET delivery_type = 'SLOW',
    estimated_delivery_minutes = 60
WHERE carrier_name LIKE N'%Tiêu chuẩn%' OR carrier_name LIKE N'%Standard%';

PRINT 'Migration completed successfully!';
GO
