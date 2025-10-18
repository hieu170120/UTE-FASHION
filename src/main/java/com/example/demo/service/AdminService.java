package com.example.demo.service;

import com.example.demo.entity.Shop;

import java.util.List;

/**
 * Service dành cho các chức năng quản trị, bắt đầu với việc quản lý Shop.
 */
public interface AdminService {

    /**
     * Lấy danh sách tất cả các Shop trong hệ thống.
     *
     * @return List các shop.
     */
    List<Shop> getAllShops();

    /**
     * Phê duyệt một Shop, cho phép shop bắt đầu hoạt động.
     *
     * @param shopId ID của shop cần phê duyệt.
     * @return Shop sau khi đã được cập nhật trạng thái.
     * @throws jakarta.persistence.EntityNotFoundException nếu không tìm thấy shop.
     */
    Shop approveShop(Integer shopId);

    /**
     * Từ chối (hoặc vô hiệu hóa) một Shop.
     *
     * @param shopId ID của shop cần từ chối/vô hiệu hóa.
     * @return Shop sau khi đã được cập nhật trạng thái.
     * @throws jakarta.persistence.EntityNotFoundException nếu không tìm thấy shop.
     */
    Shop rejectShop(Integer shopId);

}
