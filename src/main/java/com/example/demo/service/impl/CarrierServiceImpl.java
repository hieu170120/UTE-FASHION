// Impl
package com.example.demo.service.impl;

import com.example.demo.dto.CarrierDTO;
import com.example.demo.entity.Carrier;
import com.example.demo.entity.Shipper;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CarrierRepository;
import com.example.demo.repository.ShipperRepository;
import com.example.demo.service.CarrierService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarrierServiceImpl implements CarrierService {

    @Autowired
    private CarrierRepository carrierRepository;
    
    @Autowired
    private ShipperRepository shipperRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CarrierDTO> getAllCarriers() {
        return carrierRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarrierDTO> getActiveCarriers() {
        return carrierRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CarrierDTO getCarrierById(Integer id) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
        return mapToDTO(carrier);
    }

    @Override
    @Transactional
    public CarrierDTO createCarrier(CarrierDTO carrierDTO) {
        Carrier carrier = mapToEntity(carrierDTO);
        Carrier saved = carrierRepository.save(carrier);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public CarrierDTO updateCarrier(Integer id, CarrierDTO carrierDTO) {
        Carrier existing = carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
        
        // Lưu trạng thái cũ để so sánh
        boolean wasActive = existing.isActive();
        boolean newActive = carrierDTO.isActive();
        
        // Update fields
        existing.setCarrierName(carrierDTO.getCarrierName());
        existing.setDescription(carrierDTO.getDescription());
        existing.setDefaultShippingFee(carrierDTO.getDefaultShippingFee());
        existing.setContactPhone(carrierDTO.getContactPhone());
        existing.setWebsiteUrl(carrierDTO.getWebsiteUrl());
        existing.setActive(newActive);
        Carrier updated = carrierRepository.save(existing);
        
        // CASCADE LOGIC: Cập nhật tất cả shipper thuộc carrier này
        if (wasActive != newActive) {
            List<Shipper> shippers = shipperRepository.findByCarrierId(id);
            if (!shippers.isEmpty()) {
                // Nếu carrier ngưng → tất cả shipper ngưng
                // Nếu carrier bật lại → tất cả shipper bật lại
                shippers.forEach(shipper -> shipper.setActive(newActive));
                shipperRepository.saveAll(shippers);
            }
        }
        
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCarrier(Integer id) {
        if (!carrierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Carrier not found with id: " + id);
        }
        carrierRepository.deleteById(id);
    }

    @Override
    public Page<CarrierDTO> getAllCarriersPaged(Pageable pageable) {
        return carrierRepository.findAll(pageable)
                .map(this::mapToDTO);
    }
    
    @Override
    public Page<CarrierDTO> findCarriersByFilters(String search, Boolean active, Pageable pageable) {
        return carrierRepository.findByFilters(search, active, pageable)
                .map(this::mapToDTO);
    }

    private CarrierDTO mapToDTO(Carrier carrier) {
        return modelMapper.map(carrier, CarrierDTO.class);
    }

    private Carrier mapToEntity(CarrierDTO dto) {
        return modelMapper.map(dto, Carrier.class);
    }
}