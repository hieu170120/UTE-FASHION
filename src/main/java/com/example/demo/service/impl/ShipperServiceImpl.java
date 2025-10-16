package com.example.demo.service.impl;

import com.example.demo.dto.ShipperDTO;
import com.example.demo.entity.Shipper;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CarrierRepository;
import com.example.demo.repository.ShipperRepository;
import com.example.demo.service.ShipperService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipperServiceImpl implements ShipperService {

    @Autowired
    private ShipperRepository shipperRepository;

    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ShipperDTO> getAllShippers() {
        return shipperRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipperDTO> getActiveShippers() {
        return shipperRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipperDTO> getShippersByCarrier(Integer carrierId) {
        return shipperRepository.findByCarrierId(carrierId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShipperDTO getShipperById(Integer id) {
        Shipper shipper = shipperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + id));
        return mapToDTO(shipper);
    }

    @Override
    @Transactional
    public ShipperDTO createShipper(ShipperDTO shipperDTO) {
        carrierRepository.findById(shipperDTO.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + shipperDTO.getCarrierId()));
        Shipper shipper = mapToEntity(shipperDTO);
        Shipper saved = shipperRepository.save(shipper);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ShipperDTO updateShipper(Integer id, ShipperDTO shipperDTO) {
        Shipper existing = shipperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + id));
        carrierRepository.findById(shipperDTO.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + shipperDTO.getCarrierId()));
        // Update fields
        existing.setCarrier(carrierRepository.getReferenceById(shipperDTO.getCarrierId()));
        existing.setFullName(shipperDTO.getFullName());
        existing.setPhoneNumber(shipperDTO.getPhoneNumber());
        existing.setEmail(shipperDTO.getEmail());
        existing.setVehicleType(shipperDTO.getVehicleType());
        existing.setActive(shipperDTO.isActive());
        // Không update cancelCount ở đây, xử lý ở logic khác
        Shipper updated = shipperRepository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteShipper(Integer id) {
        if (!shipperRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipper not found with id: " + id);
        }
        shipperRepository.deleteById(id);
    }

    @Override
    public Page<ShipperDTO> getAllShippersPaged(Pageable pageable) {
        return shipperRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Integer getShipperIdByUserId(Integer userId) {
        return shipperRepository.findByUserUserId(userId)
                .map(Shipper::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found for user id: " + userId));
    }

    private ShipperDTO mapToDTO(Shipper shipper) {
        ShipperDTO dto = modelMapper.map(shipper, ShipperDTO.class);
        if (shipper.getCarrier() != null) {
            dto.setCarrierId(shipper.getCarrier().getId());
        }
        if (shipper.getUser() != null) {
            dto.setUserId(shipper.getUser().getUserId());
        }
        dto.setCancelCount(shipper.getCancelCount());
        return dto;
    }

    private Shipper mapToEntity(ShipperDTO dto) {
        Shipper shipper = modelMapper.map(dto, Shipper.class);
        if (dto.getCarrierId() != null) {
            shipper.setCarrier(carrierRepository.getReferenceById(dto.getCarrierId()));
        }
        // User nếu cần set
        return shipper;
    }
}