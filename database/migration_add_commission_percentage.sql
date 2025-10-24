-- Migration: Add Commission Percentage to Shops table
-- Date: 2025-10-24
-- Author: Admin
-- Description: Thêm cột commission_percentage để quản lý chiết khấu cho mỗi shop

ALTER TABLE Shops
ADD COLUMN commission_percentage DECIMAL(5, 2) DEFAULT 0.00 NOT NULL;

-- Thêm index cho việc truy vấn nhanh hơn (tùy chọn)
CREATE INDEX idx_shops_commission_percentage ON Shops(commission_percentage);

-- Log migration
INSERT INTO migration_log (migration_name, status, executed_at) 
VALUES ('migration_add_commission_percentage', 'completed', GETDATE());
