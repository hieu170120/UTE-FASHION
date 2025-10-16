-- Script để cập nhật password hash cho user admin
-- Chạy script này để cập nhật password từ plain text sang BCrypt

-- Cập nhật password hash cho user admin (password: admin123)
-- BCrypt hash của "admin123" với strength 10 (hash mới được tạo)
UPDATE Users 
SET password_hash = '$2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6'
WHERE username = 'admin';

-- Kiểm tra kết quả
SELECT user_id, username, email, password_hash, is_active 
FROM Users 
WHERE username = 'admin';

-- Thông báo
PRINT 'Password hash đã được cập nhật cho user admin';
PRINT 'Password mới: admin123 (đã được mã hóa bằng BCrypt)';
PRINT 'Hash mới: $2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6';


