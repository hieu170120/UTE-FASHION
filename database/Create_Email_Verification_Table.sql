-- Tạo bảng Email_Verifications
CREATE TABLE Email_Verifications (
    verification_id INT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(100) NOT NULL,
    otp_code NVARCHAR(6) NOT NULL,
    is_verified BIT NOT NULL DEFAULT 0,
    expires_at DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    verified_at DATETIME2 NULL
);

-- Tạo index để tối ưu hóa truy vấn
CREATE INDEX IX_Email_Verifications_Email ON Email_Verifications(email);
CREATE INDEX IX_Email_Verifications_Email_OTP ON Email_Verifications(email, otp_code);
CREATE INDEX IX_Email_Verifications_ExpiresAt ON Email_Verifications(expires_at);

PRINT 'Bảng Email_Verifications đã được tạo thành công!';

