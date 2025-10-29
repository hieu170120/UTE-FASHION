-- =============================================
-- INSERT: Thêm phương thức thanh toán bằng xu
-- =============================================

USE UTE_Fashion;
GO

-- Kiểm tra xem payment method COIN đã tồn tại chưa
IF NOT EXISTS (SELECT 1 FROM Payment_Methods WHERE method_code = 'COIN')
BEGIN
    INSERT INTO Payment_Methods (
        method_name, 
        method_code, 
        description, 
        is_active, 
        display_order
    ) VALUES (
        N'Thanh toán bằng xu',
        'COIN',
        N'Sử dụng xu tích lũy trong tài khoản để thanh toán. Số xu sẽ được trừ tương ứng với giá trị đơn hàng.',
        1,
        3
    );
    
    PRINT 'Added payment method: COIN';
END
ELSE
BEGIN
    PRINT 'Payment method COIN already exists';
END
GO

