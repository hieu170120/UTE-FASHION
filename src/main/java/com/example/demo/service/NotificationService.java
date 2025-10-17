package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Shipper;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    
    void notifyShipperNewOrder(Shipper shipper, Order order);
    
    Long getUnreadNotificationCount(Integer shipperId);
    
    List<Map<String, Object>> getShipperNotifications(Integer shipperId);
    
    void markAsRead(Integer notificationId);
    
    void markAllAsRead(Integer shipperId);
}
