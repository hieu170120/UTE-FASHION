package com.example.demo.service;

import com.example.demo.entity.Carrier;
import com.example.demo.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;

    /**
     * Lấy danh sách carrier đang hoạt động
     */
    public List<Carrier> getAllActiveCarriers() {
        return carrierRepository.findByIsActiveTrue();
    }

    /**
     * Lấy carrier theo ID
     */
    public Carrier getCarrierById(Integer carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà vận chuyển"));
    }
}
