package com.example.demo.service;

import com.example.demo.dto.ShopDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.math.BigDecimal;

public interface ShopService {
    Page<ShopDTO> getAllShops(Pageable pageable);

    List<ShopDTO> getAllShops();

    ShopDTO getShopById(Integer id);

    ShopDTO createShop(ShopDTO shopDTO, Integer vendorId);

    ShopDTO updateShop(Integer id, ShopDTO shopDTO);

    void deleteShop(Integer id);
    
    /**
     * Cập nhật chiết khấu (commission) của một shop
     * @param shopId ID của shop
     * @param commissionPercentage % chiết khấu (0-100)
     * @return ShopDTO sau khi cập nhật
     */
    ShopDTO updateShopCommission(Integer shopId, BigDecimal commissionPercentage);

    Integer getVendorIdByShopId(Integer shopId);
}
