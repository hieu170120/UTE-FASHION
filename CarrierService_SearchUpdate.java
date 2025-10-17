// UPDATED: CarrierService.java và CarrierServiceImpl.java - Thêm search

// ===== CarrierService.java =====
public interface CarrierService {
    
    // ... existing methods ...
    
    /**
     * Tìm kiếm carrier theo tên hoặc mô tả với phân trang
     */
    Page<CarrierDTO> searchCarriers(String keyword, Pageable pageable);
}

// ===== CarrierServiceImpl.java - Thêm method =====
@Service
public class CarrierServiceImpl implements CarrierService {
    
    // ... existing code ...
    
    @Override
    public Page<CarrierDTO> searchCarriers(String keyword, Pageable pageable) {
        return carrierRepository.findByCarrierNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, pageable)
                .map(this::mapToDTO);
    }
}

// ===== CarrierRepository.java - Thêm method =====
public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
    
    // ... existing methods ...
    
    /**
     * Tìm kiếm carrier theo tên hoặc mô tả (không phân biệt chữ hoa/thường)
     */
    Page<Carrier> findByCarrierNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String carrierName, String description, Pageable pageable);
    
    /**
     * Tìm các carrier đang hoạt động theo tên
     */
    List<Carrier> findByIsActiveTrueAndCarrierNameContainingIgnoreCase(String carrierName);
}