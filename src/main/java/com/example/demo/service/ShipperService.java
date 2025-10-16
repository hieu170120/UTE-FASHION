package com.example.demo.service;

import com.example.demo.dto.ShipperDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShipperService {
    List<ShipperDTO> getAllShippers();
    List<ShipperDTO> getActiveShippers();
    List<ShipperDTO> getShippersByCarrier(Integer carrierId);
    ShipperDTO getShipperById(Integer id);
    ShipperDTO createShipper(ShipperDTO shipperDTO);
    ShipperDTO updateShipper(Integer id, ShipperDTO shipperDTO);
    void deleteShipper(Integer id);
    Page<ShipperDTO> getAllShippersPaged(Pageable pageable);
    Integer getShipperIdByUserId(Integer userId);
}