package com.example.demo.service.impl;

import com.example.demo.dto.ShipperDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.Shipper;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CarrierRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.ShipperRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ShipperService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShipperServiceImpl implements ShipperService {

    @Autowired
    private ShipperRepository shipperRepository;

    @Autowired
    private CarrierRepository carrierRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Override
    public List<ShipperDTO> getAllShippers() {
        return shipperRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipperDTO> getActiveShippers() {
        return shipperRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipperDTO> getShippersByCarrier(Integer carrierId) {
        return shipperRepository.findByCarrierId(carrierId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ShipperDTO> getActiveShippersByCarrier(Integer carrierId) {
        return shipperRepository.findByCarrierIdAndIsActiveTrue(carrierId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperDTO getShipperById(Integer id) {
        // Clear cache to force fetch from database
        entityManager.clear();
        Shipper shipper = shipperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + id));
        // Refresh entity to get latest data
        entityManager.refresh(shipper);
        return mapToDTO(shipper);
    }

    @Override
    @Transactional
    public ShipperDTO createShipper(ShipperDTO shipperDTO) {
        carrierRepository.findById(shipperDTO.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + shipperDTO.getCarrierId()));
        Shipper shipper = mapToEntity(shipperDTO);
        Shipper saved = shipperRepository.save(shipper);
        return mapToDTO(saved);
    }
    
    @Override
    @Transactional
    public ShipperDTO createShipperWithUser(ShipperDTO shipperDTO, String username, String password) {
        // Validate carrier
        carrierRepository.findById(shipperDTO.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + shipperDTO.getCarrierId()));
        
        // Tạo User account
        String finalUsername = (username != null && !username.isEmpty()) ? username : shipperDTO.getEmail();
        String finalPassword = (password != null && !password.isEmpty()) ? password : "123456";
        
        // Kiểm tra username đã tồn tại
        if (userRepository.findByUsername(finalUsername).isPresent()) {
            throw new RuntimeException("Username đã tồn tại: " + finalUsername);
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(shipperDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại: " + shipperDTO.getEmail());
        }
        
        // Tạo User
        User user = new User();
        user.setUsername(finalUsername);
        user.setPasswordHash(passwordEncoder.encode(finalPassword));
        user.setEmail(shipperDTO.getEmail());
        user.setFullName(shipperDTO.getFullName());
        user.setIsActive(true);
        
        // Gán role SHIPPER
        Role shipperRole = roleRepository.findByRoleName("SHIPPER")
                .orElseThrow(() -> new ResourceNotFoundException("Role SHIPPER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(shipperRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        // Tạo Shipper và liên kết với User
        Shipper shipper = mapToEntity(shipperDTO);
        shipper.setUser(savedUser);
        Shipper savedShipper = shipperRepository.save(shipper);
        
        return mapToDTO(savedShipper);
    }

    @Override
    @Transactional
    public ShipperDTO updateShipper(Integer id, ShipperDTO shipperDTO) {
        Shipper existing = shipperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + id));
        
        com.example.demo.entity.Carrier newCarrier = carrierRepository.findById(shipperDTO.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + shipperDTO.getCarrierId()));
        
        // VALIDATION: Không cho active shipper nếu carrier đang ngưng
        if (shipperDTO.isActive() && !newCarrier.isActive()) {
            throw new RuntimeException("Không thể kích hoạt shipper khi nhà vận chuyển đang ngưng hoạt động. Vui lòng kích hoạt nhà vận chuyển trước hoặc chuyển sang nhà vận chuyển khác.");
        }
        
        // TỰ ĐỘNG ACTIVE: Nếu chuyển sang carrier khác đang hoạt động → tự động active
        Integer oldCarrierId = existing.getCarrier() != null ? existing.getCarrier().getId() : null;
        if (!shipperDTO.getCarrierId().equals(oldCarrierId) && newCarrier.isActive()) {
            shipperDTO.setActive(true);
        }
        
        // Update fields
        existing.setCarrier(newCarrier);
        existing.setFullName(shipperDTO.getFullName());
        existing.setPhoneNumber(shipperDTO.getPhoneNumber());
        existing.setEmail(shipperDTO.getEmail());
        existing.setVehicleType(shipperDTO.getVehicleType());
        existing.setActive(shipperDTO.isActive());
        // Không update cancelCount ở đây, xử lý ở logic khác
        Shipper updated = shipperRepository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteShipper(Integer id) {
        if (!shipperRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipper not found with id: " + id);
        }
        shipperRepository.deleteById(id);
    }

    @Override
    public Page<ShipperDTO> getAllShippersPaged(Pageable pageable) {
        return shipperRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Integer getShipperIdByUserId(Integer userId) {
        return shipperRepository.findByUserUserId(userId)
                .map(Shipper::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found for user id: " + userId));
    }
    
    @Override
    public Page<ShipperDTO> getAllShippersPagedWithFilters(Pageable pageable, String search, Boolean active, Integer carrierId) {
        // Lấy tất cả shippers và filter bằng stream
        List<Shipper> allShippers = shipperRepository.findAll();
        java.util.stream.Stream<Shipper> stream = allShippers.stream();
        
        // Apply search filter
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            stream = stream.filter(s -> 
                s.getFullName().toLowerCase().contains(searchLower) ||
                s.getEmail().toLowerCase().contains(searchLower) ||
                s.getPhoneNumber().contains(searchLower)
            );
        }
        
        // Apply active filter
        if (active != null) {
            stream = stream.filter(s -> s.isActive() == active);
        }
        
        // Apply carrier filter
        if (carrierId != null) {
            stream = stream.filter(s -> s.getCarrier() != null && s.getCarrier().getId().equals(carrierId));
        }
        
        // Collect filtered results
        List<Shipper> filtered = stream.collect(Collectors.toList());
        
        // Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        
        List<ShipperDTO> pageContent = start < filtered.size() ? 
            filtered.subList(start, end).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()) : 
            java.util.Collections.emptyList();
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }
    
    @Override
    @Transactional
    public void toggleShipperActive(Integer id) {
        Shipper shipper = shipperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + id));
        shipper.setActive(!shipper.isActive());
        shipperRepository.save(shipper);
    }

    private ShipperDTO mapToDTO(Shipper shipper) {
        ShipperDTO dto = modelMapper.map(shipper, ShipperDTO.class);
        if (shipper.getCarrier() != null) {
            dto.setCarrierId(shipper.getCarrier().getId());
        }
        if (shipper.getUser() != null) {
            dto.setUserId(shipper.getUser().getUserId());
        }
        dto.setCancelCount(shipper.getCancelCount());
        return dto;
    }

    private Shipper mapToEntity(ShipperDTO dto) {
        Shipper shipper = modelMapper.map(dto, Shipper.class);
        if (dto.getCarrierId() != null) {
            shipper.setCarrier(carrierRepository.getReferenceById(dto.getCarrierId()));
        }
        // User nếu cần set
        return shipper;
    }
}