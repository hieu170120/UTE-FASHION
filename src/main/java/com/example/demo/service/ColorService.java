package com.example.demo.service;

import com.example.demo.dto.ColorDTO;

import java.util.List;

public interface ColorService {
    List<ColorDTO> findAll();
    ColorDTO findById(Integer id);

}
