// Interface
package com.example.demo.service;

import com.example.demo.dto.CarrierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarrierService {
    List<CarrierDTO> getAllCarriers();
    List<CarrierDTO> getActiveCarriers();
    CarrierDTO getCarrierById(Integer id);
    CarrierDTO createCarrier(CarrierDTO carrierDTO);
    CarrierDTO updateCarrier(Integer id, CarrierDTO carrierDTO);
    void deleteCarrier(Integer id);
    Page<CarrierDTO> getAllCarriersPaged(Pageable pageable); // Nếu cần paging
}