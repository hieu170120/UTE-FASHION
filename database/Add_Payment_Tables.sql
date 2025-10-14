-- =============================================
-- Script: Add Payment Tables
-- Mô tả: Thêm bảng Payment_Methods và Payments nếu chưa tồn tại
-- Phiên bản: 1.0
-- Ngày: 2025-01-14
-- =============================================

USE UTE_Fashion;
GO

PRINT '=== KIỂM TRA VÀ TẠO PAYMENT TABLES ===';
PRINT '';

-- =============================================
-- BƯỚC 1: Kiểm tra và tạo bảng Payment_Methods
-- =============================================

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payment_Methods]') AND type in (N'U'))
BEGIN
    PRINT 'Bảng Payment_Methods chưa tồn tại. Đang tạo...';
    
    CREATE TABLE Payment_Methods (
        payment_method_id INT PRIMARY KEY IDENTITY(1,1),
        method_name NVARCHAR(100) NOT NULL UNIQUE,
        method_code NVARCHAR(50) NOT NULL UNIQUE, -- 'COD', 'SEPAY_QR', 'VNPAY', 'MOMO'
        description NVARCHAR(500),
        icon_url NVARCHAR(500),
        is_active BIT DEFAULT 1,
        display_order INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );
    
    PRINT '✅ Đã tạo bảng Payment_Methods thành công!';
END
ELSE
BEGIN
    PRINT '✅ Bảng Payment_Methods đã tồn tại.';
END
GO

-- =============================================
-- BƯỚC 2: Kiểm tra và tạo bảng Payments
-- =============================================

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payments]') AND type in (N'U'))
BEGIN
    PRINT 'Bảng Payments chưa tồn tại. Đang tạo...';
    
    CREATE TABLE Payments (
        payment_id INT PRIMARY KEY IDENTITY(1,1),
        order_id INT NOT NULL,
        payment_method_id INT NOT NULL,
        transaction_id NVARCHAR(255), -- ID từ payment gateway
        amount DECIMAL(18,2) NOT NULL,
        payment_status NVARCHAR(50) NOT NULL DEFAULT 'Pending', 
        -- 'Pending', 'Success', 'Failed', 'Refunded'
        payment_gateway_response NVARCHAR(MAX), -- JSON response từ SePay
        paid_at DATETIME,
        refunded_at DATETIME,
        refund_amount DECIMAL(18,2),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
        FOREIGN KEY (payment_method_id) REFERENCES Payment_Methods(payment_method_id)
    );
    
    PRINT '✅ Đã tạo bảng Payments thành công!';
END
ELSE
BEGIN
    PRINT '✅ Bảng Payments đã tồn tại.';
END
GO

-- =============================================
-- BƯỚC 3: Kiểm tra kết quả
-- =============================================

PRINT '';
PRINT '=== KIỂM TRA CẤU TRÚC BẢNG ===';
PRINT '';

-- Kiểm tra Payment_Methods
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payment_Methods]') AND type in (N'U'))
BEGIN
    PRINT '✓ Payment_Methods: EXISTS';
    SELECT 
        COLUMN_NAME,
        DATA_TYPE,
        CHARACTER_MAXIMUM_LENGTH,
        IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Payment_Methods'
    ORDER BY ORDINAL_POSITION;
END
ELSE
BEGIN
    PRINT '✗ Payment_Methods: NOT EXISTS';
END
GO

-- Kiểm tra Payments
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Payments]') AND type in (N'U'))
BEGIN
    PRINT '';
    PRINT '✓ Payments: EXISTS';
    SELECT 
        COLUMN_NAME,
        DATA_TYPE,
        CHARACTER_MAXIMUM_LENGTH,
        IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Payments'
    ORDER BY ORDINAL_POSITION;
END
ELSE
BEGIN
    PRINT '✗ Payments: NOT EXISTS';
END
GO

PRINT '';
PRINT '=== HOÀN THÀNH ===';
PRINT 'Script Add_Payment_Tables.sql đã chạy thành công!';
PRINT '';
PRINT 'Bước tiếp theo: Chạy Insert_Payment_Methods.sql để thêm dữ liệu.';
PRINT '';
GO
