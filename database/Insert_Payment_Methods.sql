-- =============================================
-- Script: Insert Payment Methods
-- Mô tả: Thêm các phương thức thanh toán (COD và SePay QR)
-- Phiên bản: 1.0
-- Ngày: 2025-01-14
-- =============================================

USE UTE_Fashion;
GO

-- =============================================
-- BƯỚC 1: Kiểm tra và xóa dữ liệu cũ
-- =============================================

PRINT 'Bắt đầu insert Payment Methods...';
PRINT '';

-- Xóa dữ liệu Payment Methods cũ (nếu có)
DELETE FROM Payment_Methods;
GO

PRINT 'Đã xóa dữ liệu Payment Methods cũ.';
GO

-- =============================================
-- BƯỚC 2: Insert Payment Methods
-- =============================================

-- COD - Cash on Delivery (Thanh toán khi nhận hàng)
INSERT INTO Payment_Methods (
    method_name, 
    method_code, 
    description, 
    is_active, 
    display_order
) VALUES (
    N'Thanh toán khi nhận hàng (COD)',
    'COD',
    N'Thanh toán bằng tiền mặt khi nhận hàng. Bạn sẽ trả tiền trực tiếp cho nhân viên giao hàng.',
    1,
    1
);

-- SePay QR - QR Code Banking
INSERT INTO Payment_Methods (
    method_name, 
    method_code, 
    description, 
    is_active, 
    display_order
) VALUES (
    N'Chuyển khoản QR Code',
    'SEPAY_QR',
    N'Quét mã QR để thanh toán qua ngân hàng BIDV. Đơn hàng sẽ được tự động xác nhận trong vòng 60 giây.',
    1,
    2
);

GO

PRINT 'Đã insert 2 Payment Methods thành công.';
PRINT '';
GO

-- =============================================
-- BƯỚC 3: Kiểm tra kết quả
-- =============================================

PRINT '=== DANH SÁCH PAYMENT METHODS ===';
PRINT '';

SELECT 
    payment_method_id,
    method_name,
    method_code,
    description,
    is_active,
    display_order,
    created_at,
    updated_at
FROM Payment_Methods
ORDER BY display_order;
GO

-- =============================================
-- BƯỚC 4: Thông tin tóm tắt
-- =============================================

DECLARE @TotalCount INT;
DECLARE @ActiveCount INT;

SELECT @TotalCount = COUNT(*) FROM Payment_Methods;
SELECT @ActiveCount = COUNT(*) FROM Payment_Methods WHERE is_active = 1;

PRINT '';
PRINT '=== THỐNG KÊ ===';
PRINT 'Tổng số Payment Methods: ' + CAST(@TotalCount AS NVARCHAR(10));
PRINT 'Số Payment Methods đang hoạt động: ' + CAST(@ActiveCount AS NVARCHAR(10));
PRINT '';
PRINT '=== CHI TIẾT PAYMENT METHODS ===';
PRINT '';
PRINT '1. COD (Cash on Delivery)';
PRINT '   - Mã: COD';
PRINT '   - Mô tả: Thanh toán khi nhận hàng';
PRINT '   - Trạng thái: Đang hoạt động';
PRINT '   - Payment Status: Unpaid (sau khi đặt hàng)';
PRINT '   - Payment Record Status: Pending';
PRINT '';
PRINT '2. SePay QR (QR Banking)';
PRINT '   - Mã: SEPAY_QR';
PRINT '   - Mô tả: Thanh toán qua QR Code';
PRINT '   - Ngân hàng: BIDV';
PRINT '   - Số tài khoản: 96247NMB0A';
PRINT '   - Trạng thái: Đang hoạt động';
PRINT '   - Payment Status: Paid (sau khi verify)';
PRINT '   - Payment Record Status: Success';
PRINT '   - Polling: 3s interval, 60s timeout';
PRINT '';
PRINT '=== HOÀN THÀNH ===';
PRINT 'Script Insert_Payment_Methods.sql đã chạy thành công!';
PRINT '';
GO
