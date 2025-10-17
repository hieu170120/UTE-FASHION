package com.example.demo.service.impl;

import com.example.demo.dto.ShopRegistrationDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.VendorService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional
    public Shop registerNewShop(ShopRegistrationDTO registrationDTO) {
        // 1. Lấy thông tin user đang đăng nhập
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + currentUsername));

        // 2. Kiểm tra xem user đã là VENDOR hoặc đã có shop chưa
        boolean isVendor = currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals("VENDOR"));
        if (isVendor) {
            throw new IllegalStateException("Bạn đã là người bán hàng rồi.");
        }
        if (shopRepository.findByVendorId(currentUser.getId()).isPresent()) {
            throw new IllegalStateException("Bạn đã có một cửa hàng được đăng ký.");
        }
        if (shopRepository.existsByShopName(registrationDTO.getShopName())) {
            throw new IllegalStateException("Tên cửa hàng đã tồn tại.");
        }

        // 3. Lấy vai trò VENDOR
        Role vendorRole = roleRepository.findByRoleName("VENDOR")
                .orElseThrow(() -> new IllegalStateException("Vai trò 'VENDOR' không tồn tại trong hệ thống."));

        // 4. Cập nhật vai trò cho user
        currentUser.getRoles().add(vendorRole);
        userRepository.save(currentUser);

        // 5. Tạo shop mới
        Shop newShop = new Shop();
        newShop.setVendor(currentUser);
        newShop.setShopName(registrationDTO.getShopName());
        newShop.setSlug(slugify.slugify(registrationDTO.getShopName()));
        newShop.setDescription(registrationDTO.getDescription());
        newShop.setLogoUrl(registrationDTO.getLogoUrl());
        newShop.setActive(false); // Chờ admin duyệt

        return shopRepository.save(newShop);
    }

    @Override
    public Optional<Shop> getCurrentVendorShop() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + currentUsername));

        return shopRepository.findByVendorId(currentUser.getId());
    }

    @Override
    @Transactional
    public Shop updateCurrentVendorShop(ShopRegistrationDTO shopDTO) {
        Shop existingShop = getCurrentVendorShop()
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Bạn chưa có cửa hàng để cập nhật."));

        // Kiểm tra nếu tên shop mới đã tồn tại và không phải là tên của shop hiện tại
        if (!existingShop.getShopName().equals(shopDTO.getShopName()) && shopRepository.existsByShopName(shopDTO.getShopName())) {
            throw new IllegalStateException("Tên cửa hàng đã tồn tại.");
        }

        existingShop.setShopName(shopDTO.getShopName());
        existingShop.setSlug(slugify.slugify(shopDTO.getShopName()));
        existingShop.setDescription(shopDTO.getDescription());
        if (shopDTO.getLogoUrl() != null && !shopDTO.getLogoUrl().isEmpty()) {
            existingShop.setLogoUrl(shopDTO.getLogoUrl());
        }

        return shopRepository.save(existingShop);
    }
}
