package com.example.demo.scheduler;

import com.example.demo.entity.Order;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task để tự động cập nhật đơn hàng sang trạng thái "Đã Giao"
 * khi hết thời gian đếm ngược (estimated_delivery_time)
 */
@Component
public class OrderDeliveryScheduler {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderManagementService orderManagementService;

    /**
     * Chạy mỗi 30 giây để kiểm tra các đơn hàng đang giao
     */
    @Scheduled(fixedRate = 30000) // 30 giây
    public void checkAndUpdateDeliveredOrders() {
        try {
            // Lấy tất cả đơn hàng đang ở trạng thái "SHIPPING"
            List<Order> deliveringOrders = orderRepository.findByOrderStatus(
                    OrderStatus.SHIPPING.getValue());
            
            LocalDateTime now = LocalDateTime.now();
            
            for (Order order : deliveringOrders) {
                // Kiểm tra nếu đã đến thời gian giao hàng dự kiến
                if (order.getEstimatedDeliveryTime() != null && 
                    now.isAfter(order.getEstimatedDeliveryTime())) {
                    
                    // Tự động đánh dấu là đã giao
                    orderManagementService.markOrderAsDelivered(order.getId());
                    
                    System.out.println("✅ Đơn hàng #" + order.getOrderNumber() + 
                            " đã được tự động cập nhật sang trạng thái 'Đã Giao'");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }
}
