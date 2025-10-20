package com.example.demo.service;

import com.example.demo.dto.BrandDTO;
import com.example.demo.dto.CategoryDTO;

import java.util.List;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    List<BrandDTO> findAllActive();
    BrandDTO getBrandById(Integer id);
    BrandDTO getBrandBySlug(String slug);
    BrandDTO createBrand(BrandDTO brandDTO);
    BrandDTO updateBrand(Integer id, BrandDTO brandDTO);
    void deleteBrand(Integer id);
}
