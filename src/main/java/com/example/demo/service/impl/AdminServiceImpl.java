package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Shop;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.AdminShopService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminShopService {

	private final ShopRepository shopRepository;

	@Override
	public List<Shop> getAllShops() {
		return shopRepository.findAllWithVendor();
	}

	@Override
	@Transactional
	public Shop approveShop(Integer shopId) {
		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cửa hàng với ID: " + shopId));
		shop.setActive(true);
		return shopRepository.save(shop);
	}

	@Override
	@Transactional
	public Shop rejectShop(Integer shopId) {
		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cửa hàng với ID: " + shopId));
		shop.setActive(false);
		return shopRepository.save(shop);
	}
}
