# Test Avatar Upload Script
# Chạy script này để test chức năng upload avatar

Write-Host "=== TEST AVATAR UPLOAD ===" -ForegroundColor Green

# Tạo một file ảnh test nhỏ
$testImagePath = "test_image.jpg"
if (-not (Test-Path $testImagePath)) {
    Write-Host "Tạo file ảnh test..." -ForegroundColor Yellow
    # Tạo một file ảnh đơn giản (1x1 pixel JPEG)
    $bytes = [byte[]](0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32, 0xFF, 0xC0, 0x00, 0x11, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0x02, 0x11, 0x01, 0x03, 0x11, 0x01, 0xFF, 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0xFF, 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xDA, 0x00, 0x0C, 0x03, 0x01, 0x02, 0x11, 0x03, 0x11, 0x00, 0x3F, 0x00, 0x00, 0xFF, 0xD9)
    [System.IO.File]::WriteAllBytes($testImagePath, $bytes)
}

Write-Host "File ảnh test: $testImagePath" -ForegroundColor Cyan

# Test upload với curl
Write-Host "`n=== TESTING UPLOAD WITH CURL ===" -ForegroundColor Green

$curlCommand = @"
curl -X POST `
  -F "avatarFile=@$testImagePath" `
  -H "Cookie: UTE_FASHION_SESSION=your_session_id" `
  -v `
  http://localhost:5055/UTE_Fashion/profile/upload-avatar
"@

Write-Host "Command: $curlCommand" -ForegroundColor Yellow
Write-Host "`nLưu ý: Bạn cần đăng nhập trước và lấy session ID từ browser" -ForegroundColor Red

# Test endpoint kiểm tra avatar
Write-Host "`n=== TESTING AVATAR CHECK ENDPOINT ===" -ForegroundColor Green
$checkCommand = "curl http://localhost:5055/UTE_Fashion/profile/test-avatar"
Write-Host "Command: $checkCommand" -ForegroundColor Yellow

Write-Host "`n=== HƯỚNG DẪN TEST ===" -ForegroundColor Green
Write-Host "1. Đăng nhập vào ứng dụng tại: http://localhost:5055/UTE_Fashion/login" -ForegroundColor White
Write-Host "2. Mở Developer Tools (F12) và lấy session cookie" -ForegroundColor White
Write-Host "3. Thay thế 'your_session_id' trong command curl bằng session thực tế" -ForegroundColor White
Write-Host "4. Chạy command curl để upload avatar" -ForegroundColor White
Write-Host "5. Kiểm tra endpoint /profile/test-avatar để xem avatar URL" -ForegroundColor White
Write-Host "6. Kiểm tra console logs của ứng dụng Spring Boot" -ForegroundColor White

Write-Host "`n=== KIỂM TRA LOGS ===" -ForegroundColor Green
Write-Host "Mở console của ứng dụng Spring Boot để xem debug logs:" -ForegroundColor White
Write-Host "- DEBUG: Uploading avatar for user: [username]" -ForegroundColor Cyan
Write-Host "- DEBUG: Generated avatar URL: [url]" -ForegroundColor Cyan
Write-Host "- DEBUG: File saved to: [path]" -ForegroundColor Cyan
Write-Host "- DEBUG: Updating avatar URL from '[old]' to '[new]'" -ForegroundColor Cyan
Write-Host "- DEBUG: Saving user to database..." -ForegroundColor Cyan
Write-Host "- DEBUG: User saved successfully. Avatar URL in DB: [url]" -ForegroundColor Cyan


