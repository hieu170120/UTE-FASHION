package com.example.demo.service.impl;

import com.example.demo.dto.ShopDTO;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {
	
	private static final Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

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
    
    @Override
    @Transactional
    public ShopDTO updateShopCommission(Integer shopId, java.math.BigDecimal commissionPercentage) {
        logger.info("🔵 [ShopServiceImpl] updateShopCommission - START");
        logger.info("   shopId: {}, newCommission: {}", shopId, commissionPercentage);
        
        try {
            logger.info("📍 [ShopServiceImpl] Finding shop with id: {}", shopId);
            Shop existingShop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
            logger.info("✅ [ShopServiceImpl] Found shop: {}", existingShop.getShopName());
            
            // Validate commission percentage
            logger.info("📍 [ShopServiceImpl] Validating commission percentage: {}", commissionPercentage);
            if (commissionPercentage.compareTo(java.math.BigDecimal.ZERO) < 0 || 
                commissionPercentage.compareTo(new java.math.BigDecimal("100")) > 0) {
                logger.error("❌ [ShopServiceImpl] Invalid commission percentage: {}. Must be 0-100", commissionPercentage);
                throw new IllegalArgumentException("Chiết khấu phải nằm trong khoảng 0-100%");
            }
            logger.info("✅ [ShopServiceImpl] Commission percentage is valid");
            
            // Update commission
            logger.info("📍 [ShopServiceImpl] Setting new commission from {} to {}", 
                       existingShop.getCommissionPercentage(), commissionPercentage);
            existingShop.setCommissionPercentage(commissionPercentage);
            
            // Save to database
            logger.info("📍 [ShopServiceImpl] Saving shop to database");
            Shop updatedShop = shopRepository.save(existingShop);
            logger.info("✅ [ShopServiceImpl] Shop saved successfully. Commission is now: {}", 
                       updatedShop.getCommissionPercentage());
            
            // Convert to DTO
            ShopDTO resultDTO = modelMapper.map(updatedShop, ShopDTO.class);
            logger.info("✅ [ShopServiceImpl] updateShopCommission - SUCCESS");
            
            return resultDTO;
        } catch (ResourceNotFoundException e) {
            logger.error("❌ [ShopServiceImpl] ResourceNotFoundException: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("❌ [ShopServiceImpl] IllegalArgumentException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ShopServiceImpl] Unexpected Exception: {}", e.getMessage());
            logger.error("   Full stack trace:", e);
            throw new RuntimeException("Error updating shop commission: " + e.getMessage(), e);}
        }

    @Override
    @Transactional(readOnly = true)
    public Integer getVendorIdByShopId(Integer shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        User vendor = shop.getVendor();
        if (vendor == null) {
            throw new ResourceNotFoundException("Vendor not found for shop with id: " + shopId);
        }

        return vendor.getUserId();
    }
}
