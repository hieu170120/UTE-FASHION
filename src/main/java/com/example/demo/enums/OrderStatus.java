package com.example.demo.enums;

public enum OrderStatus {
    PENDING("Pending"),                      // Chờ thanh toán
    PROCESSING("Processing"),                // Đang xử lý - Chờ Admin xác nhận
    CONFIRMED("Confirmed"),                  // Đã xử lý - Admin đã chọn Shipper
    SHIPPING("Shipping"),                    // Đang giao - Shipper đã xác nhận giao
    DELIVERED("Delivered"),                  // Đã giao
    CANCELLED("Cancelled"),                  // Đã hủy
    RETURN_REQUESTED("Return_Requested"),    // Yêu cầu trả hàng
    RETURNED("Returned");                    // Đã trả hàng

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}