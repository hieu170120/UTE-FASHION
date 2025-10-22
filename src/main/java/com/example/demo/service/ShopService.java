package com.example.demo.service;

import com.example.demo.dto.ShopDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShopService {
    Page<ShopDTO> getAllShops(Pageable pageable);

    List<ShopDTO> getAllShops();

    ShopDTO getShopById(Integer id);

    ShopDTO createShop(ShopDTO shopDTO, Integer vendorId);

    ShopDTO updateShop(Integer id, ShopDTO shopDTO);

    void deleteShop(Integer id);

    Integer getVendorIdByShopId(Integer shopId);
}
