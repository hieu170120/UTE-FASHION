package com.example.demo.service.impl;

import com.example.demo.entity.Order;
import com.example.demo.entity.Shipper;
import com.example.demo.entity.ShipperNotification;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ShipperNotificationRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private ShipperNotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notifyShipperNewOrder(Shipper shipper, Order order) {
        ShipperNotification notification = new ShipperNotification();
        notification.setShipper(shipper);
        notification.setOrder(order);
        notification.setMessage(String.format(
            "Bạn đã được giao đơn hàng #%s. Vui lòng xác nhận hoặc từ chối đơn hàng.",
            order.getOrderNumber()
        ));
        notification.setRead(false);
        
        notificationRepository.save(notification);
    }

    @Override
    public Long getUnreadNotificationCount(Integer shipperId) {
        return notificationRepository.countByShipperIdAndIsReadFalse(shipperId);
    }

    @Override
    public List<Map<String, Object>> getShipperNotifications(Integer shipperId) {
        List<ShipperNotification> notifications = notificationRepository
            .findByShipperIdOrderByCreatedAtDesc(shipperId);
        
        return notifications.stream()
            .map(notification -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", notification.getId());
                map.put("title", "Đơn hàng mới");
                map.put("message", notification.getMessage());
                map.put("isRead", notification.isRead());
                map.put("createdAt", notification.getCreatedAt());
                map.put("timeAgo", formatTimeAgo(notification.getCreatedAt()));
                map.put("orderId", notification.getOrder().getId());
                map.put("orderNumber", notification.getOrder().getOrderNumber());
                return map;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {
        ShipperNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer shipperId) {
        List<ShipperNotification> notifications = notificationRepository
            .findByShipperIdAndIsReadFalseOrderByCreatedAtDesc(shipperId);
        
        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(notifications);
    }
    
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Vừa xong";
        }
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            return dateTime.toString();
        }
    }
}
