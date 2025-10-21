package com.example.demo.service;

import com.example.demo.dto.ShipperDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShipperService {
    List<ShipperDTO> getAllShippers();
    List<ShipperDTO> getActiveShippers();
    List<ShipperDTO> getShippersByCarrier(Integer carrierId);
    List<ShipperDTO> getActiveShippersByCarrier(Integer carrierId);
    ShipperDTO getShipperById(Integer id);
    ShipperDTO createShipper(ShipperDTO shipperDTO);
    ShipperDTO createShipperWithUser(ShipperDTO shipperDTO, String username, String password);
    ShipperDTO updateShipper(Integer id, ShipperDTO shipperDTO);
    void deleteShipper(Integer id);
    Page<ShipperDTO> getAllShippersPaged(Pageable pageable);
    Page<ShipperDTO> getAllShippersPagedWithFilters(Pageable pageable, String search, Boolean active, Integer carrierId);
    void toggleShipperActive(Integer id);
    Integer getShipperIdByUserId(Integer userId);
}
