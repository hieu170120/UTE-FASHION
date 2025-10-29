package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderReturnRequestDTO;
import com.example.demo.dto.ShipperCancelHistoryDTO;

import java.util.List;

public interface OrderManagementService {
    
    // === VENDOR FUNCTIONS ===
    
    /**
     * Vendor xác nhận có hàng và chuyển đơn sang trạng thái Vendor_Confirmed
     * @param orderId ID đơn hàng
     * @param shopId ID cửa hàng vendor
     */
    void vendorConfirmOrder(Integer orderId, Integer shopId);
    
    /**
     * Vendor từ chối đơn (không còn hàng hoặc lý do khác)
     * @param orderId ID đơn hàng
     * @param shopId ID cửa hàng vendor
     * @param reason Lý do từ chối
     */
    void vendorRejectOrder(Integer orderId, Integer shopId, String reason);
    
    // === ADMIN FUNCTIONS ===
    
    /**
     * Lấy danh sách đơn hàng đang chờ xử lý (Vendor_Confirmed - chờ admin chọn shipper)
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
    void rejectReturnRequest(Integer requestId, String rejectionReason);
    
    
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
     * Lấy danh sách lịch sử hủy đơn của shipper
     */
    List<ShipperCancelHistoryDTO> getShipperCancelHistory(Integer shipperId);
    
    /**
     * Lấy lịch sử hủy của một đơn hàng cụ thể (cho admin xem)
     */
    List<ShipperCancelHistoryDTO> getOrderCancelHistory(Integer orderId);
    
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
    
    /**
     * Shipper xác nhận đã nhận tiền COD và hoàn thành giao hàng
     * Cập nhật orderStatus = Delivered và paymentStatus = Paid
     */
    void shipperConfirmCODPayment(Integer orderId, Integer shipperId);
    
    /**
     * Shipper báo không giao được hàng
     * Cập nhật orderStatus về Processing để admin xử lý
     */
    void shipperMarkAsFailedDelivery(Integer orderId, Integer shipperId, String reason);
    
    
    // === CUSTOMER FUNCTIONS ===
    
    /**
     * Khách hàng hủy đơn (chỉ khi DON_DANG_XU_LY hoặc DA_XU_LY_DON_HANG)
     */
    void customerCancelOrder(Integer orderId, Integer userId, String cancelReason);
    
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
