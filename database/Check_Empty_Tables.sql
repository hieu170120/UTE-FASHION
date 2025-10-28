-- =============================================
-- CHECK EMPTY TABLES - KIỂM TRA BẢNG TRỐNG
-- =============================================
-- Script kiểm tra toàn bộ bảng trong UTE_Fashion database
-- Hiển thị danh sách bảng kèm số lượng hàng
-- Dễ nhìn thấy bảng nào không có data

USE UTE_Fashion;
GO

PRINT '=========================================='
PRINT 'KIỂM TRA CÁC BẢNG TRỐNG TRONG DATABASE'
PRINT '=========================================='
PRINT ''

-- =============================================
-- PHƯƠNG PHÁP 1: Hiển thị TẤT CẢ bảng + số hàng
-- =============================================
PRINT '📊 BẢNG 1: TẤT CẢ CÁC BẢNG TRONG DATABASE'
PRINT '=========================================='

SELECT 
    ROW_NUMBER() OVER (ORDER BY row_count ASC) AS 'STT',
    table_name AS 'Tên bảng',
    row_count AS 'Số hàng',
    CASE 
        WHEN row_count = 0 THEN '❌ TRỐNG (0 hàng)'
        WHEN row_count < 5 THEN '⚠️  ÍT DỮ LIỆU'
        ELSE '✅ CÓ DỮ LIỆU'
    END AS 'Trạng thái'
FROM (
    SELECT 
        t.name AS table_name,
        SUM(p.rows) AS row_count
    FROM sys.tables t
    INNER JOIN sys.indexes i ON t.object_id = i.object_id
    INNER JOIN sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id
    WHERE t.name NOT LIKE 'sys%'
    GROUP BY t.name
) AS table_counts
ORDER BY row_count ASC;

PRINT ''
PRINT ''

-- =============================================
-- PHƯƠNG PHÁP 2: Chỉ hiển thị CÁC BẢNG TRỐNG
-- =============================================
PRINT '❌ BẢNG 2: CHỈ CÁC BẢNG TRỐNG (0 hàng)'
PRINT '=========================================='

SELECT 
    ROW_NUMBER() OVER (ORDER BY t.name ASC) AS 'STT',
    t.name AS 'Tên bảng'
FROM sys.tables t
INNER JOIN sys.indexes i ON t.object_id = i.object_id
INNER JOIN sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id
WHERE t.name NOT LIKE 'sys%'
GROUP BY t.name
HAVING SUM(p.rows) = 0
ORDER BY t.name;

PRINT ''
PRINT ''

-- =============================================
-- PHƯƠNG PHÁP 3: Kiểm tra các bảng cụ thể
-- =============================================
PRINT '🔍 BẢNG 3: KIỂM TRA CÁC BẢNG QUAN TRỌNG'
PRINT '=========================================='

SELECT 
    'Roles' AS 'Tên bảng', COUNT(*) AS 'Số hàng',
    CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END AS 'Trạng thái'
FROM Roles
UNION ALL
SELECT 'Users', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Users
UNION ALL
SELECT 'User_Roles', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM User_Roles
UNION ALL
SELECT 'Shops', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Shops
UNION ALL
SELECT 'Categories', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Categories
UNION ALL
SELECT 'Brands', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Brands
UNION ALL
SELECT 'Products', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Products
UNION ALL
SELECT 'Sizes', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Sizes
UNION ALL
SELECT 'Colors', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Colors
UNION ALL
SELECT 'Product_Variants', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Product_Variants
UNION ALL
SELECT 'Product_Images', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Product_Images
UNION ALL
SELECT 'Carriers', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Carriers
UNION ALL
SELECT 'Shippers', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Shippers
UNION ALL
SELECT 'Orders', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Orders
UNION ALL
SELECT 'Payments', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Payments
UNION ALL
SELECT 'Coupons', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Coupons
UNION ALL
SELECT 'Reviews', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Reviews
UNION ALL
SELECT 'Wishlists', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Wishlists
UNION ALL
SELECT 'Carts', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Carts
UNION ALL
SELECT 'Cart_Items', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Cart_Items
UNION ALL
SELECT 'Addresses', COUNT(*), CASE WHEN COUNT(*) = 0 THEN '❌ TRỐNG' ELSE '✅ CÓ' END FROM Addresses
ORDER BY 'Tên bảng';

PRINT ''
PRINT ''

-- =============================================
-- PHƯƠNG PHÁP 4: Thống kê tổng quan
-- =============================================
PRINT '📈 BẢNG 4: THỐNG KÊ TỔNG QUAN'
PRINT '=========================================='

DECLARE @TotalTables INT;
DECLARE @EmptyTables INT;
DECLARE @TablesWithData INT;

SELECT @TotalTables = COUNT(*)
FROM sys.tables 
WHERE name NOT LIKE 'sys%';

SELECT @EmptyTables = COUNT(*)
FROM (
    SELECT t.name
    FROM sys.tables t
    INNER JOIN sys.indexes i ON t.object_id = i.object_id
    INNER JOIN sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id
    WHERE t.name NOT LIKE 'sys%'
    GROUP BY t.name
    HAVING SUM(p.rows) = 0
) AS empty_tables;

SELECT @TablesWithData = @TotalTables - @EmptyTables;

SELECT 
    @TotalTables AS 'Tổng số bảng',
    @EmptyTables AS 'Bảng trống (0 hàng)',
    @TablesWithData AS 'Bảng có dữ liệu',
    CAST(@EmptyTables * 100.0 / @TotalTables AS DECIMAL(5,2)) AS 'Tỷ lệ trống (%)';

PRINT ''
PRINT '=========================================='
PRINT '✅ KIỂM TRA XONG'
PRINT '=========================================='
GO

-- ============================================================================
-- COMMISSION CALCULATION VERIFICATION QUERIES
-- ============================================================================
-- Ngày: 2025-10-27
-- Mục đích: Kiểm tra các cột commission_percentage, commission_amount, 
--           shop_net_revenue trong database

-- ============================================================================
-- 1. KIỂM TRA CẤU TRÚC BẢNG SHOPS
-- ============================================================================

PRINT '✅ [Step 1] Checking Shops table structure for commission_percentage'
DESC Shops;
-- Expected to see: commission_percentage DECIMAL(5, 2)

-- Kiểm tra giá trị mặc định
SELECT COUNT(*) as total_shops,
       SUM(CASE WHEN commission_percentage IS NULL THEN 1 ELSE 0 END) as null_commission,
       SUM(CASE WHEN commission_percentage = 0.00 THEN 1 ELSE 0 END) as zero_commission
FROM Shops;

-- Hiển thị tất cả shops và commission của chúng
PRINT '✅ Current Shop Commission Settings:'
SELECT shop_id, shop_name, commission_percentage
FROM Shops
ORDER BY shop_id;


-- ============================================================================
-- 2. KIỂM TRA CẤU TRÚC BẢNG SHOPANALYTICS
-- ============================================================================

PRINT '✅ [Step 2] Checking ShopAnalytics table structure for commission fields'
DESC ShopAnalytics;
-- Expected to see: commission_percentage, commission_amount, shop_net_revenue

-- Kiểm tra dữ liệu commission trong ShopAnalytics
PRINT '✅ ShopAnalytics Commission Data (Last 10 records):'
SELECT TOP 10
    analytics_id,
    shop_id,
    period_start,
    period_end,
    total_revenue,
    total_orders,
    commission_percentage,
    commission_amount,
    shop_net_revenue,
    (commission_amount + shop_net_revenue) as verification_sum
FROM ShopAnalytics
WHERE commission_amount > 0
ORDER BY analytics_id DESC;


-- ============================================================================
-- 3. KIỂM TRA LOGIC TÍNH TOÁN - VERIFICATION SUM
-- ============================================================================

PRINT '✅ [Step 3] Verifying Commission Calculation Logic'
PRINT '   (Commission Amount + Shop Net Revenue should equal Total Revenue)'

SELECT
    analytics_id,
    shop_id,
    total_revenue,
    commission_amount,
    shop_net_revenue,
    (commission_amount + shop_net_revenue) as calculated_total,
    CASE 
        WHEN ABS(total_revenue - (commission_amount + shop_net_revenue)) < 0.01 THEN '✅ CORRECT'
        ELSE '❌ MISMATCH'
    END as verification_result
FROM ShopAnalytics
WHERE total_revenue > 0 AND commission_amount > 0
ORDER BY analytics_id DESC;


-- ============================================================================
-- 4. KIỂM TRA THEO SHOP - TỔNG CHIẾT KHẤU
-- ============================================================================

PRINT '✅ [Step 4] Total Commission by Shop (Cumulative Today)'

SELECT
    s.shop_id,
    s.shop_name,
    s.commission_percentage,
    COUNT(sa.analytics_id) as number_of_records,
    SUM(sa.total_revenue) as total_revenue_sum,
    SUM(sa.total_orders) as total_orders_sum,
    SUM(sa.commission_amount) as total_commission_sum,
    SUM(sa.shop_net_revenue) as total_shop_net_revenue_sum
FROM Shops s
LEFT JOIN ShopAnalytics sa ON s.shop_id = sa.shop_id 
    AND sa.period_type = 'DAY'
    AND sa.period_start = CAST(GETDATE() AS DATE)
GROUP BY s.shop_id, s.shop_name, s.commission_percentage
ORDER BY s.shop_id;


-- ============================================================================
-- 5. KIỂM TRA ORDERS ĐÃ DELIVERED
-- ============================================================================

PRINT '✅ [Step 5] Orders Delivered Today (Should Trigger Commission Calculation)'

SELECT
    o.order_id,
    o.order_number,
    o.shop_id,
    s.shop_name,
    o.total_amount,
    s.commission_percentage,
    (o.total_amount * s.commission_percentage / 100) as expected_commission,
    o.delivered_at,
    o.order_status
FROM Orders o
LEFT JOIN Shops s ON o.shop_id = s.shop_id
WHERE o.order_status = 'Delivered'
    AND CAST(o.delivered_at AS DATE) = CAST(GETDATE() AS DATE)
ORDER BY o.order_id DESC;


-- ============================================================================
-- 6. KIỂM TRA LỖI - ORDERS KHÔNG CÓ SHOP
-- ============================================================================

PRINT '✅ [Step 6] Checking for Orders without Shop (Edge Case)'

SELECT COUNT(*) as orders_without_shop
FROM Orders
WHERE shop_id IS NULL;

-- Nếu > 0, các orders này sẽ không được tính chiết khấu


-- ============================================================================
-- 7. KIỂM TRA SHOPS KHÔNG CÓ COMMISSION SETTING
-- ============================================================================

PRINT '✅ [Step 7] Checking Shops without Commission Setting'

SELECT
    shop_id,
    shop_name,
    commission_percentage,
    CASE
        WHEN commission_percentage IS NULL THEN '❌ NULL'
        WHEN commission_percentage = 0.00 THEN '⚠️ ZERO (No Commission)'
        ELSE '✅ Set to ' + CAST(commission_percentage AS VARCHAR)
    END as status
FROM Shops
ORDER BY shop_id;


-- ============================================================================
-- 8. RESET COMMISSION DATA (KHI CẦN TEST LẠI)
-- ============================================================================

/*
-- UNCOMMMENT NẾU CẦN XÓA DỮ LIỆU COMMISSION ĐỂ TEST LẠI

PRINT '⚠️ [DANGER] Clearing ShopAnalytics Commission Data'

-- Xóa ShopAnalytics hôm nay
DELETE FROM ShopAnalytics
WHERE period_type = 'DAY' 
    AND period_start = CAST(GETDATE() AS DATE);

-- Reset commission_percentage về 0 (nếu muốn test với 0%)
-- UPDATE Shops SET commission_percentage = 0.00;

-- Reset commission_percentage thành 10% (để test)
-- UPDATE Shops SET commission_percentage = 10.00;

PRINT '✅ Data cleared. You can now re-run the order delivery flow to recalculate.'
*/


-- ============================================================================
-- 9. MONITORING - REAL-TIME COMMISSION TRACKING
-- ============================================================================

PRINT '✅ [Step 9] Real-Time Commission Summary'

SELECT
    'Total Shops' as metric,
    COUNT(*) as value
FROM Shops
UNION ALL
SELECT 'Total Shops with Commission > 0', COUNT(*) 
FROM Shops WHERE commission_percentage > 0
UNION ALL
SELECT 'Shops Analyzed Today', COUNT(DISTINCT shop_id)
FROM ShopAnalytics
WHERE period_start = CAST(GETDATE() AS DATE)
UNION ALL
SELECT 'Total Revenue Today', SUM(total_revenue)
FROM ShopAnalytics
WHERE period_start = CAST(GETDATE() AS DATE)
UNION ALL
SELECT 'Total Commission Today (Platform Revenue)', SUM(commission_amount)
FROM ShopAnalytics
WHERE period_start = CAST(GETDATE() AS DATE)
UNION ALL
SELECT 'Total Shop Net Revenue Today', SUM(shop_net_revenue)
FROM ShopAnalytics
WHERE period_start = CAST(GETDATE() AS DATE);


-- ============================================================================
PRINT '✅ ============ COMMISSION VERIFICATION COMPLETE ============'
-- ============================================================================