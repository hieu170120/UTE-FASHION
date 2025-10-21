-- Script để fix dữ liệu User thiếu createdAt và lastLogin
-- Chạy script này để cập nhật dữ liệu

-- 1. Cập nhật createdAt cho các user chưa có
UPDATE Users 
SET created_at = GETDATE() - 30  -- Đặt ngày tạo cách đây 30 ngày
WHERE created_at IS NULL;

-- 2. Cập nhật lastLogin cho các user đã từng đăng nhập (có thể đặt ngẫu nhiên)
UPDATE Users 
SET last_login = GETDATE() - CAST(RAND() * 7 AS INT)  -- Đặt ngày đăng nhập cuối cách đây 0-7 ngày
WHERE last_login IS NULL 
AND username IN ('admin', 'user01', 'user02'); -- Chỉ cập nhật các user đã biết

-- 3. Kiểm tra kết quả
SELECT 
    user_id,
    username,
    email,
    full_name,
    created_at,
    last_login,
    is_active,
    is_email_verified
FROM Users 
ORDER BY created_at DESC;

