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
