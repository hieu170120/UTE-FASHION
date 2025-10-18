package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderReturnRequestDTO;

import java.util.List;

public interface OrderManagementService {
    
    // === ADMIN FUNCTIONS ===
    
    /**
     * Lấy danh sách đơn hàng đang chờ xử lý (DON_DANG_XU_LY)
     */
    List<OrderDTO> getPendingOrders();
    
    /**
     * Admin xác nhận đơn và chọn shipper
     * @param orderId ID đơn hàng
     * @param shipperId ID shipper được chọn
     */
    void adminConfirmOrderAndAssignShipper(Integer orderId, Integer shipperId);
    
    /**
     * Lấy danh sách yêu cầu trả hàng chờ xử lý
     */
    List<OrderReturnRequestDTO> getPendingReturnRequests();
    
    /**
     * Admin phê duyệt yêu cầu trả hàng
     */
    void approveReturnRequest(Integer requestId);
    
    /**
     * Admin từ chối yêu cầu trả hàng
     */
    void rejectReturnRequest(Integer requestId);
    
    
    // === SHIPPER FUNCTIONS ===
    
    /**
     * Lấy danh sách đơn được giao cho shipper (DA_XU_LY_DON_HANG)
     */
    List<OrderDTO> getShipperAssignedOrders(Integer shipperId);
    
    /**
     * Shipper xác nhận nhận đơn và bắt đầu giao (DA_XU_LY_DON_HANG -> DANG_GIAO)
     * Bắt đầu đếm ngược random 2-5 phút
     */
    void shipperConfirmOrder(Integer orderId, Integer shipperId);
    
    /**
     * Shipper hủy đơn (nếu chưa vượt quá 3 lần)
     * Đơn quay về trạng thái DON_DANG_XU_LY để admin chọn shipper khác
     */
    void shipperCancelOrder(Integer orderId, Integer shipperId, String reason);
    
    /**
     * Kiểm tra số lần shipper đã hủy đơn
     */
    Long getShipperCancelCount(Integer shipperId);
    
    /**
     * Lấy danh sách đơn đang giao của shipper (DANG_GIAO)
     */
    List<OrderDTO> getShipperDeliveringOrders(Integer shipperId);
    
    /**
     * Lấy tất cả đơn hàng của shipper (tất cả trạng thái)
     */
    List<OrderDTO> getShipperAllOrders(Integer shipperId);
    
    /**
     * Tự động cập nhật đơn sang DA_GIAO khi hết thời gian đếm ngược
     */
    void markOrderAsDelivered(Integer orderId);
    
    
    // === CUSTOMER FUNCTIONS ===
    
    /**
     * Khách hàng hủy đơn (chỉ khi DON_DANG_XU_LY hoặc DA_XU_LY_DON_HANG)
     */
    void customerCancelOrder(Integer orderId, Integer userId);
    
    /**
     * Khách hàng yêu cầu trả hàng (chỉ khi DA_GIAO)
     */
    void customerRequestReturn(Integer orderId, Integer userId, String reason);
    
    /**
     * Lấy lịch sử đơn hàng của khách hàng
     */
    List<OrderDTO> getCustomerOrders(Integer userId);
    
    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    OrderDTO getOrderById(Integer orderId);
}
