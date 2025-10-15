-- Script để cập nhật password hash cho user admin
-- Chạy script này để cập nhật password từ plain text sang BCrypt

-- Cập nhật password hash cho user admin (password: admin123)
-- BCrypt hash của "admin123" với strength 10
UPDATE Users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';

-- Kiểm tra kết quả
SELECT user_id, username, email, password_hash, is_active 
FROM Users 
WHERE username = 'admin';

-- Thông báo
PRINT 'Password hash đã được cập nhật cho user admin';
PRINT 'Password mới: admin123 (đã được mã hóa bằng BCrypt)';
