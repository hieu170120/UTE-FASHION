package com.example.demo.service;

import com.example.demo.dto.ShopRegistrationDTO;
import com.example.demo.entity.Shop;

import java.util.Optional;

/**
 * Service này xử lý các nghiệp vụ dành riêng cho Vendor (Người bán).
 */
public interface VendorService {

    /**
     * Đăng ký một shop mới cho người dùng đang đăng nhập.
     *
     * @param registrationDTO Dữ liệu đăng ký shop từ form.
     * @return Đối tượng Shop đã được tạo và lưu vào DB (trạng thái chờ duyệt).
     * @throws IllegalStateException nếu người dùng đã có vai trò VENDOR hoặc đã có shop.
     */
    Shop registerNewShop(ShopRegistrationDTO registrationDTO);

    /**
     * Lấy thông tin shop của người dùng (vendor) đang đăng nhập.
     *
     * @return Optional chứa thông tin Shop nếu người dùng này có shop, ngược lại trả về Optional rỗng.
     */
    Optional<Shop> getCurrentVendorShop();

    /**
     * Cho phép vendor cập nhật thông tin shop của chính họ.
     *
     * @param shopDTO Dữ liệu mới để cập nhật cho shop.
     * @return Đối tượng Shop sau khi đã được cập nhật.
     * @throws java.nio.file.AccessDeniedException nếu người dùng không sở hữu shop này.
     * @throws jakarta.persistence.EntityNotFoundException nếu không tìm thấy shop.
     */
    Shop updateCurrentVendorShop(ShopRegistrationDTO shopDTO);
    
    /**
     * Lấy ID của shop dựa vào username của vendor.
     *
     * @param username Tên đăng nhập của vendor.
     * @return ID của shop nếu tìm thấy.
     */
    Integer getShopIdByUsername(String username);

}
