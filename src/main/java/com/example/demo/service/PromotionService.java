package com.example.demo.service;

import com.example.demo.dto.PromotionDTO;
import com.example.demo.dto.PromotionRuleDTO;
import com.example.demo.dto.PromotionStatisticsDTO;
import com.example.demo.entity.Promotion;
import com.example.demo.entity.PromotionRule;
import com.example.demo.entity.PromotionUsage;
import com.example.demo.entity.User;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.PromotionRuleRepository;
import com.example.demo.repository.PromotionUsageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionRuleRepository promotionRuleRepository;

    @Autowired
    private PromotionUsageRepository promotionUsageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tìm kiếm và phân trang promotions
     */
    @Transactional(readOnly = true)
    public Page<PromotionDTO> searchPromotions(String keyword, Boolean isActive, 
                                             Promotion.PromotionType promotionType, Pageable pageable) {
        Page<Promotion> promotionPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            promotionPage = promotionRepository.findByKeyword(keyword.trim(), pageable);
        } else if (isActive != null) {
            promotionPage = promotionRepository.findByIsActive(isActive, pageable);
        } else if (promotionType != null) {
            promotionPage = promotionRepository.findByPromotionType(promotionType, pageable);
        } else {
            promotionPage = promotionRepository.findAll(pageable);
        }
        
        return promotionPage.map(this::mapToPromotionDTO);
    }

    /**
     * Lấy promotion theo ID
     */
    @Transactional(readOnly = true)
    public PromotionDTO getPromotionById(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return mapToPromotionDTO(promotion);
    }

    /**
     * Tạo promotion mới
     */
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO, Integer createdByUserId) {
        // Validate
        validatePromotion(promotionDTO);
        
        // Check duplicate name
        if (promotionRepository.findByPromotionName(promotionDTO.getPromotionName()).isPresent()) {
            throw new RuntimeException("Tên chương trình khuyến mãi đã tồn tại");
        }
        
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create promotion
        Promotion promotion = new Promotion();
        promotion.setPromotionName(promotionDTO.getPromotionName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setPromotionType(promotionDTO.getPromotionType());
        promotion.setDiscountType(promotionDTO.getDiscountType());
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setMinOrderValue(promotionDTO.getMinOrderValue() != null ? promotionDTO.getMinOrderValue() : BigDecimal.ZERO);
        promotion.setMaxDiscountAmount(promotionDTO.getMaxDiscountAmount());
        promotion.setUsageLimit(promotionDTO.getUsageLimit());
        promotion.setUsageLimitPerUser(promotionDTO.getUsageLimitPerUser() != null ? promotionDTO.getUsageLimitPerUser() : 1);
        promotion.setValidFrom(promotionDTO.getValidFrom());
        promotion.setValidTo(promotionDTO.getValidTo());
        promotion.setIsActive(promotionDTO.isActive());
        promotion.setPriority(promotionDTO.getPriority() != null ? promotionDTO.getPriority() : 0);
        promotion.setCreatedBy(createdBy);
        
        promotion = promotionRepository.save(promotion);
        
        // Create rules
        if (promotionDTO.getRules() != null && !promotionDTO.getRules().isEmpty()) {
            for (PromotionRuleDTO ruleDTO : promotionDTO.getRules()) {
                PromotionRule rule = new PromotionRule();
                rule.setPromotion(promotion);
                rule.setRuleType(ruleDTO.getRuleType());
                rule.setRuleValue(ruleDTO.getRuleValue());
                rule.setOperator(ruleDTO.getOperator());
                promotionRuleRepository.save(rule);
            }
        }
        
        return mapToPromotionDTO(promotion);
    }

    /**
     * Cập nhật promotion
     */
    @Transactional
    public PromotionDTO updatePromotion(Integer id, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        // Validate
        validatePromotion(promotionDTO);
        
        // Check duplicate name (exclude current promotion)
        Optional<Promotion> existingPromotion = promotionRepository.findByPromotionName(promotionDTO.getPromotionName());
        if (existingPromotion.isPresent() && !existingPromotion.get().getId().equals(id)) {
            throw new RuntimeException("Tên chương trình khuyến mãi đã tồn tại");
        }
        
        // Update promotion
        promotion.setPromotionName(promotionDTO.getPromotionName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setPromotionType(promotionDTO.getPromotionType());
        promotion.setDiscountType(promotionDTO.getDiscountType());
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setMinOrderValue(promotionDTO.getMinOrderValue() != null ? promotionDTO.getMinOrderValue() : BigDecimal.ZERO);
        promotion.setMaxDiscountAmount(promotionDTO.getMaxDiscountAmount());
        promotion.setUsageLimit(promotionDTO.getUsageLimit());
        promotion.setUsageLimitPerUser(promotionDTO.getUsageLimitPerUser() != null ? promotionDTO.getUsageLimitPerUser() : 1);
        promotion.setValidFrom(promotionDTO.getValidFrom());
        promotion.setValidTo(promotionDTO.getValidTo());
        promotion.setIsActive(promotionDTO.isActive());
        promotion.setPriority(promotionDTO.getPriority() != null ? promotionDTO.getPriority() : 0);
        
        promotion = promotionRepository.save(promotion);
        
        // Update rules - delete old rules and create new ones
        promotionRuleRepository.deleteByPromotionId(id);
        if (promotionDTO.getRules() != null && !promotionDTO.getRules().isEmpty()) {
            for (PromotionRuleDTO ruleDTO : promotionDTO.getRules()) {
                PromotionRule rule = new PromotionRule();
                rule.setPromotion(promotion);
                rule.setRuleType(ruleDTO.getRuleType());
                rule.setRuleValue(ruleDTO.getRuleValue());
                rule.setOperator(ruleDTO.getOperator());
                promotionRuleRepository.save(rule);
            }
        }
        
        return mapToPromotionDTO(promotion);
    }

    /**
     * Xóa promotion
     */
    @Transactional
    public void deletePromotion(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        // Check if promotion has been used
        long usageCount = promotionUsageRepository.countByPromotionId(id);
        if (usageCount > 0) {
            throw new RuntimeException("Không thể xóa chương trình khuyến mãi đã được sử dụng");
        }
        
        promotionRepository.delete(promotion);
    }

    /**
     * Kích hoạt/vô hiệu hóa promotion
     */
    @Transactional
    public void togglePromotionStatus(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        promotion.setIsActive(!promotion.isActive());
        promotionRepository.save(promotion);
    }

    /**
     * Lấy thống kê promotions
     */
    @Transactional(readOnly = true)
    public PromotionStatisticsDTO getPromotionStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        
        PromotionStatisticsDTO stats = new PromotionStatisticsDTO();
        
        // Thống kê tổng quan
        stats.setTotalPromotions(promotionRepository.count());
        stats.setActivePromotions(promotionRepository.countActivePromotions());
        stats.setTotalUsage(promotionRepository.getTotalUsageCount() != null ? promotionRepository.getTotalUsageCount() : 0L);
        
        // Thống kê theo thời gian
        List<PromotionUsage> recentUsage = promotionUsageRepository.findUsageByDateRange(
                now.minusMonths(6), now);
        
        Map<String, Long> usageByMonth = recentUsage.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getUsedAt().getYear() + "-" + String.format("%02d", usage.getUsedAt().getMonthValue()),
                        Collectors.counting()
                ));
        stats.setUsageByMonth(usageByMonth);
        
        // Top promotions
        List<Object[]> topPromotionsData = promotionUsageRepository.findTopPromotionsByUsage(
                now.minusMonths(1), now);
        
        List<PromotionStatisticsDTO.PromotionSummaryDTO> topPromotions = topPromotionsData.stream()
                .limit(5)
                .map(data -> {
                    PromotionStatisticsDTO.PromotionSummaryDTO summary = new PromotionStatisticsDTO.PromotionSummaryDTO();
                    summary.setId((Integer) data[0]);
                    summary.setUsageCount((Long) data[1]);
                    return summary;
                })
                .collect(Collectors.toList());
        stats.setTopPromotions(topPromotions);
        
        // Promotions sắp hết hạn
        List<Promotion> expiringSoon = promotionRepository.findPromotionsExpiringSoon(now, sevenDaysLater);
        List<PromotionStatisticsDTO.PromotionSummaryDTO> expiringSoonDTOs = expiringSoon.stream()
                .map(promotion -> {
                    PromotionStatisticsDTO.PromotionSummaryDTO summary = new PromotionStatisticsDTO.PromotionSummaryDTO();
                    summary.setId(promotion.getId());
                    summary.setPromotionName(promotion.getPromotionName());
                    summary.setValidTo(promotion.getValidTo());
                    return summary;
                })
                .collect(Collectors.toList());
        stats.setExpiringSoon(expiringSoonDTOs);
        
        return stats;
    }

    /**
     * Validate promotion data
     */
    private void validatePromotion(PromotionDTO promotionDTO) {
        if (promotionDTO.getPromotionName() == null || promotionDTO.getPromotionName().trim().isEmpty()) {
            throw new RuntimeException("Tên chương trình khuyến mãi không được để trống");
        }
        
        if (promotionDTO.getValidFrom() == null || promotionDTO.getValidTo() == null) {
            throw new RuntimeException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        
        if (promotionDTO.getValidFrom().isAfter(promotionDTO.getValidTo())) {
            throw new RuntimeException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        
        if (promotionDTO.getDiscountValue() == null || promotionDTO.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm giá phải lớn hơn 0");
        }
        
        if (promotionDTO.getDiscountType() == Promotion.DiscountType.PERCENTAGE && 
            promotionDTO.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Giảm giá theo phần trăm không được vượt quá 100%");
        }
    }

    /**
     * Map Promotion entity to DTO
     */
    private PromotionDTO mapToPromotionDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(promotion.getId());
        dto.setPromotionName(promotion.getPromotionName());
        dto.setDescription(promotion.getDescription());
        dto.setPromotionType(promotion.getPromotionType());
        dto.setDiscountType(promotion.getDiscountType());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setMinOrderValue(promotion.getMinOrderValue());
        dto.setMaxDiscountAmount(promotion.getMaxDiscountAmount());
        dto.setUsageLimit(promotion.getUsageLimit());
        dto.setUsageCount(promotion.getUsageCount());
        dto.setUsageLimitPerUser(promotion.getUsageLimitPerUser());
        dto.setValidFrom(promotion.getValidFrom());
        dto.setValidTo(promotion.getValidTo());
        dto.setActive(promotion.isActive());
        dto.setPriority(promotion.getPriority());
        dto.setCreatedBy(promotion.getCreatedBy().getUsername());
        dto.setCreatedAt(promotion.getCreatedAt());
        dto.setUpdatedAt(promotion.getUpdatedAt());
        
        // Map rules
        if (promotion.getRules() != null) {
            List<PromotionRuleDTO> ruleDTOs = promotion.getRules().stream()
                    .map(this::mapToPromotionRuleDTO)
                    .collect(Collectors.toList());
            dto.setRules(ruleDTOs);
        }
        
        // Set status
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive()) {
            dto.setStatus("INACTIVE");
        } else if (promotion.getValidFrom().isAfter(now)) {
            dto.setStatus("UPCOMING");
        } else if (promotion.getValidTo().isBefore(now)) {
            dto.setStatus("EXPIRED");
        } else {
            dto.setStatus("ACTIVE");
            dto.setExpiringSoon(promotion.getValidTo().isBefore(now.plusDays(7)));
        }
        
        return dto;
    }

    /**
     * Map PromotionRule entity to DTO
     */
    private PromotionRuleDTO mapToPromotionRuleDTO(PromotionRule rule) {
        PromotionRuleDTO dto = new PromotionRuleDTO();
        dto.setId(rule.getId());
        dto.setPromotionId(rule.getPromotion().getId());
        dto.setRuleType(rule.getRuleType());
        dto.setRuleValue(rule.getRuleValue());
        dto.setOperator(rule.getOperator());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setRuleTypeDisplayName(rule.getRuleType().getDisplayName());
        dto.setOperatorDisplayName(rule.getOperator().getDisplayName());
        return dto;
    }
    
    /**
     * Lấy danh sách promotions đang active và còn hiệu lực (cho cart total)
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getAvailablePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findAll().stream()
                .filter(p -> p.isActive() && 
                           !p.getValidFrom().isAfter(now) && 
                           !p.getValidTo().isBefore(now) &&
                           p.getPromotionType() == Promotion.PromotionType.CART_TOTAL &&
                           (p.getUsageLimit() == null || p.getUsageCount() < p.getUsageLimit()))
                .collect(Collectors.toList());
        
        return promotions.stream()
                .map(this::mapToPromotionDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Validate promotion và kiểm tra điều kiện áp dụng
     */
    @Transactional(readOnly = true)
    public PromotionDTO validatePromotionForUser(Integer promotionId, Integer userId, BigDecimal orderTotal) {
        LocalDateTime now = LocalDateTime.now();
        
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại"));
        
        // Kiểm tra trạng thái active
        if (!promotion.isActive()) {
            throw new RuntimeException("Mã khuyến mãi đã bị vô hiệu hóa");
        }
        
        // Kiểm tra thời gian
        if (promotion.getValidFrom().isAfter(now)) {
            throw new RuntimeException("Mã khuyến mãi chưa đến thời gian áp dụng");
        }
        if (promotion.getValidTo().isBefore(now)) {
            throw new RuntimeException("Mã khuyến mãi đã hết hạn");
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (orderTotal.compareTo(promotion.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu " + 
                formatMoney(promotion.getMinOrderValue()) + " để áp dụng mã này");
        }
        
        // Kiểm tra usage limit tổng
        if (promotion.getUsageLimit() != null && 
            promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw new RuntimeException("Mã khuyến mãi đã hết lượt sử dụng");
        }
        
        // Kiểm tra usage limit per user
        if (userId != null && promotion.getUsageLimitPerUser() != null) {
            long userUsageCount = promotionUsageRepository.countByPromotionIdAndUserUserId(
                promotion.getId(), userId);
            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                throw new RuntimeException("Bạn đã sử dụng hết số lượt cho mã này");
            }
        }
        
        return mapToPromotionDTO(promotion);
    }
    
    /**
     * Tính số tiền giảm giá từ promotion
     */
    public BigDecimal calculateDiscount(PromotionDTO promotion, BigDecimal orderTotal) {
        BigDecimal discount = BigDecimal.ZERO;
        
        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            // Giảm theo %
            discount = orderTotal.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            
            // Áp dụng max discount nếu có
            if (promotion.getMaxDiscountAmount() != null && 
                discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED_AMOUNT) {
            // Giảm số tiền cố định
            discount = promotion.getDiscountValue();
        }
        
        // Không giảm quá tổng tiền
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        
        return discount;
    }
    
    /**
     * Format tiền
     */
    private String formatMoney(BigDecimal amount) {
        return String.format("%,d₫", amount.longValue());
    }
}
