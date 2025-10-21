package com.example.demo.service.impl;

import com.example.demo.dto.ColorDTO;
import com.example.demo.entity.Color;
import com.example.demo.repository.ColorRepository;
import com.example.demo.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorServiceImpl implements ColorService {

    @Autowired
    private ColorRepository colorRepository;

    @Override
    public List<ColorDTO> findAll() {
        return colorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ColorDTO convertToDto(Color color) {
        ColorDTO dto = new ColorDTO();
        dto.setId(color.getId());
        dto.setColorName(color.getColorName());
        dto.setColorCode(color.getColorCode());
        return dto;
    }
}
