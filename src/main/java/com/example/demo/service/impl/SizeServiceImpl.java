package com.example.demo.service.impl;

import com.example.demo.dto.SizeDTO;
import com.example.demo.entity.Size;
import com.example.demo.repository.SizeRepository;
import com.example.demo.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SizeServiceImpl implements SizeService {

    @Autowired
    private SizeRepository sizeRepository;

    @Override
    public List<SizeDTO> findAll() {
        return sizeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public SizeDTO findById(Integer id) {
        return sizeRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    private SizeDTO convertToDto(Size size) {
        SizeDTO dto = new SizeDTO();
        dto.setId(size.getId());
        dto.setSizeName(size.getSizeName());
        dto.setSizeType(size.getSizeType());
        return dto;
    }
}
