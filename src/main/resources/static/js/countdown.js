/**
 * Countdown Timer for Shipper Order Delivery
 * Đếm ngược thời gian giao hàng cho shipper
 */

function initCountdown(orderId, countdownSeconds, deliveryStartedAt) {
    const element = document.getElementById(`countdown-${orderId}`);
    const completeButton = document.getElementById(`complete-btn-${orderId}`);
    
    if (!element) return;
    
    // Tính thời gian còn lại
    const startTime = new Date(deliveryStartedAt).getTime();
    const endTime = startTime + (countdownSeconds * 1000);
    
    function updateCountdown() {
        const now = new Date().getTime();
        const distance = endTime - now;
        
        if (distance < 0) {
            // Hết giờ
            element.innerHTML = '<span class="badge bg-success">⏰ Đã đến nơi</span>';
            if (completeButton) {
                completeButton.disabled = false;
                completeButton.classList.remove('btn-secondary');
                completeButton.classList.add('btn-success');
                completeButton.innerHTML = '✓ Đã giao hàng';
            }
            return;
        }
        
        // Tính phút và giây
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);
        
        // Hiển thị
        element.innerHTML = `
            <div class="countdown-timer">
                <i class="fas fa-clock"></i> 
                <strong>${minutes}:${seconds < 10 ? '0' + seconds : seconds}</strong>
            </div>
        `;
    }
    
    // Cập nhật ngay lập tức
    updateCountdown();
    
    // Cập nhật mỗi giây
    const interval = setInterval(function() {
        updateCountdown();
        
        // Kiểm tra nếu hết giờ thì dừng
        const now = new Date().getTime();
        if (now >= endTime) {
            clearInterval(interval);
        }
    }, 1000);
}

/**
 * Init tất cả countdown timers trên trang
 */
function initAllCountdowns() {
    const countdownElements = document.querySelectorAll('[data-countdown]');
    countdownElements.forEach(element => {
        const orderId = element.dataset.orderId;
        const countdownSeconds = parseInt(element.dataset.countdownSeconds);
        const deliveryStartedAt = element.dataset.deliveryStartedAt;
        
        if (orderId && countdownSeconds && deliveryStartedAt) {
            initCountdown(orderId, countdownSeconds, deliveryStartedAt);
        }
    });
}

// Auto init khi DOM ready
document.addEventListener('DOMContentLoaded', initAllCountdowns);
