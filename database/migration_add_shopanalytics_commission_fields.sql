-- Migration: Add Commission Fields to ShopAnalytics table
-- Date: 2025-10-24
-- Description: Thêm các cột commission_percentage, commission_amount, shop_net_revenue vào bảng ShopAnalytics

ALTER TABLE ShopAnalytics
ADD COLUMN commission_percentage DECIMAL(5, 2) DEFAULT 0.00 NOT NULL;

ALTER TABLE ShopAnalytics
ADD COLUMN commission_amount DECIMAL(18, 2) DEFAULT 0.00 NOT NULL;

ALTER TABLE ShopAnalytics
ADD COLUMN shop_net_revenue DECIMAL(18, 2) DEFAULT 0.00 NOT NULL;

-- Tạo index để tối ưu truy vấn
CREATE INDEX idx_shopanalytics_commission_amount ON ShopAnalytics(commission_amount);
CREATE INDEX idx_shopanalytics_shop_net_revenue ON ShopAnalytics(shop_net_revenue);
