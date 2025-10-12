package com.example.demo.service.impl;

import com.example.demo.dto.BrandDTO;
import com.example.demo.entity.Brand;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BrandRepository;
import com.example.demo.service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brand -> modelMapper.map(brand, BrandDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return modelMapper.map(brand, BrandDTO.class);
    }

    @Override
    public BrandDTO getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Brand not found with slug: " + slug));
        return modelMapper.map(brand, BrandDTO.class);
    }

    @Override
    public BrandDTO createBrand(BrandDTO brandDTO) {
        Brand brand = modelMapper.map(brandDTO, Brand.class);
        Brand savedBrand = brandRepository.save(brand);
        return modelMapper.map(savedBrand, BrandDTO.class);
    }

    @Override
    public BrandDTO updateBrand(Integer id, BrandDTO brandDTO) {
        Brand existingBrand = brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        existingBrand.setBrandName(brandDTO.getBrandName());
        existingBrand.setSlug(brandDTO.getSlug());
        existingBrand.setDescription(brandDTO.getDescription());
        existingBrand.setLogoUrl(brandDTO.getLogoUrl());
        existingBrand.setWebsiteUrl(brandDTO.getWebsiteUrl());
        existingBrand.setActive(brandDTO.isActive());
        Brand updatedBrand = brandRepository.save(existingBrand);
        return modelMapper.map(updatedBrand, BrandDTO.class);
    }

    @Override
    public void deleteBrand(Integer id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }
}
