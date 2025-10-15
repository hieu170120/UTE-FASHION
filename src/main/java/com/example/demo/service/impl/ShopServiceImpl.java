package com.example.demo.service.impl;

import com.example.demo.dto.ShopDTO;
import com.example.demo.entity.Shop;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ShopService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ShopDTO> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable).map(shop -> modelMapper.map(shop, ShopDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopDTO> getAllShops() {
        return shopRepository.findAll().stream()
                .map(shop -> modelMapper.map(shop, ShopDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDTO getShopById(Integer id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
        return modelMapper.map(shop, ShopDTO.class);
    }

    @Override
    @Transactional
    public ShopDTO createShop(ShopDTO shopDTO, Integer vendorId) {
        Shop shop = modelMapper.map(shopDTO, Shop.class);
        shop.setVendor(userRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        Shop savedShop = shopRepository.save(shop);
        return modelMapper.map(savedShop, ShopDTO.class);
    }

    @Override
    @Transactional
    public ShopDTO updateShop(Integer id, ShopDTO shopDTO) {
        Shop existingShop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
        modelMapper.map(shopDTO, existingShop);
        Shop updatedShop = shopRepository.save(existingShop);
        return modelMapper.map(updatedShop, ShopDTO.class);
    }

    @Override
    public void deleteShop(Integer id) {
        if (!shopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }
}
