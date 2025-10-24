package com.example.demo.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.entity.Promotion;
import com.example.demo.entity.User;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import com.example.demo.repository.PromotionRuleRepository;
import com.example.demo.repository.PromotionUsageRepository;

/**
 * Controller quản lý chương trình khuyến mãi cho Admin
 */
@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminPromotionController.class);
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private PromotionRuleRepository promotionRuleRepository;
    
    @Autowired
    private PromotionUsageRepository promotionUsageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Trang danh sách chương trình khuyến mãi
     */
    @GetMapping
    public String listPromotions(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String isActive,
                                @RequestParam(required = false) String promotionType,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        logger.info("=== ListPromotions called ===");
        logger.info("Search: {}, IsActive: {}, Type: {}", search, isActive, promotionType);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Promotion> promotionsPage;
            
            // Tìm kiếm và lọc
            if (search != null && !search.isEmpty()) {
                logger.info("Searching by keyword: {}", search);
                promotionsPage = promotionRepository.findByKeyword(search, pageable);
            } else if (isActive != null && !isActive.isEmpty()) {
                boolean active = "true".equals(isActive);
                logger.info("Filtering by active status: {}", active);
                promotionsPage = promotionRepository.findByIsActive(active, pageable);
            } else {
                logger.info("Getting all promotions");
                promotionsPage = promotionRepository.findAll(pageable);
            }
            
            logger.info("Total promotions: {}, Current page items: {}", 
                promotionsPage.getTotalElements(), promotionsPage.getContent().size());
            
            model.addAttribute("message", "Module quản lý chương trình khuyến mãi đã sẵn sàng!");
            model.addAttribute("promotions", promotionsPage.getContent());
            model.addAttribute("searchQuery", search);
            model.addAttribute("isActiveFilter", isActive);
            model.addAttribute("promotionTypeFilter", promotionType);
            model.addAttribute("currentPage", promotionsPage.getNumber());
            model.addAttribute("totalPages", promotionsPage.getTotalPages());
            model.addAttribute("totalElements", promotionsPage.getTotalElements());
            
        } catch (Exception e) {
            logger.error("Error in listPromotions: ", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "admin/promotion/list";
    }

    /**
     * Trang tạo chương trình khuyến mãi mới
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        logger.info("=== ShowCreateForm called ===");
        try {
            model.addAttribute("message", "Tạo chương trình khuyến mãi mới");
            
            // Tạo Promotion entity mới
            Promotion promotion = new Promotion();
            promotion.setValidFrom(LocalDateTime.now());
            promotion.setValidTo(LocalDateTime.now().plusDays(30));
            
            model.addAttribute("promotionDTO", promotion);
        } catch (Exception e) {
            logger.error("Error in showCreateForm: ", e);
        }
        return "admin/promotion/form";
    }

    /**
     * Trang chỉnh sửa chương trình khuyến mãi
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        logger.info("=== ShowEditForm called with ID: {} ===", id);
        try {
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);
            if (promotionOpt.isEmpty()) {
                logger.warn("Promotion not found with ID: {}", id);
                model.addAttribute("errorMessage", "Không tìm thấy chương trình khuyến mãi với ID: " + id);
                return "redirect:/admin/promotions";
            }
            
            Promotion promotion = promotionOpt.get();
            logger.info("Found promotion: ID={}, Name={}", promotion.getId(), promotion.getPromotionName());
            
            model.addAttribute("message", "Chỉnh sửa chương trình khuyến mãi #" + id);
            model.addAttribute("promotionDTO", promotion);
        } catch (Exception e) {
            logger.error("Error in showEditForm: ", e);
        }
        return "admin/promotion/form";
    }

    /**
     * Trang chi tiết chương trình khuyến mãi
     */
    @GetMapping("/{id}")
    public String showPromotionDetail(@PathVariable Integer id, Model model) {
        logger.info("=== ShowPromotionDetail called with ID: {} ===", id);
        try {
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);
            if (promotionOpt.isEmpty()) {
                logger.warn("Promotion not found for detail view, ID: {}", id);
                return "redirect:/admin/promotions";
            }
            
            Promotion promotion = promotionOpt.get();
            logger.info("Showing details for promotion: {}", promotion.getPromotionName());
            model.addAttribute("promotion", promotion);
            model.addAttribute("message", "Chi tiết chương trình khuyến mãi #" + id);
        } catch (Exception e) {
            logger.error("Error in showPromotionDetail: ", e);
        }
        return "admin/promotion/detail";
    }

    /**
     * Xử lý form tạo/sửa chương trình khuyến mãi
     */
    @PostMapping("/save")
    public String savePromotion(@RequestParam(required = false) Integer id,
                               @RequestParam String promotionName,
                               @RequestParam String description,
                               @RequestParam String promotionType,
                               @RequestParam String discountType,
                               @RequestParam String discountValue,
                               @RequestParam(required = false) String minOrderValue,
                               @RequestParam(required = false) String maxDiscountAmount,
                               @RequestParam(required = false) String usageLimit,
                               @RequestParam(defaultValue = "1") String usageLimitPerUser,
                               @RequestParam String validFrom,
                               @RequestParam String validTo,
                               @RequestParam(defaultValue = "false") boolean isActive,
                               @RequestParam(defaultValue = "0") String priority,
                               RedirectAttributes redirectAttributes) {
        
        logger.info("=== SavePromotion called ===");
        logger.info("ID: {}, Name: {}, Type: {}, Active: {}", id, promotionName, promotionType, isActive);
        
        try {
            // Validation - Name
            if (promotionName == null || promotionName.trim().isEmpty()) {
                logger.warn("Validation failed: Promotion name is empty");
                throw new IllegalArgumentException("Tên chương trình không được để trống");
            }
            
            // Validation - Discount Value
            if (discountValue == null || discountValue.trim().isEmpty()) {
                logger.warn("Validation failed: Discount value is empty");
                throw new IllegalArgumentException("Giá trị giảm không được để trống");
            }
            
            // Parse and validate discount value
            BigDecimal discount = new BigDecimal(discountValue);
            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Validation failed: Invalid discount value: {}", discount);
                throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
            }
            
            // Convert PromotionType enum
            Promotion.PromotionType promotionTypeEnum;
            try {
                promotionTypeEnum = Promotion.PromotionType.valueOf(promotionType);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid promotion type: {}", promotionType);
                throw new IllegalArgumentException("Loại chương trình không hợp lệ: " + promotionType);
            }
            
            // Convert DiscountType enum
            Promotion.DiscountType discountTypeEnum;
            try {
                discountTypeEnum = Promotion.DiscountType.valueOf(discountType);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid discount type: {}", discountType);
                throw new IllegalArgumentException("Loại giảm giá không hợp lệ: " + discountType);
            }
            
            Promotion promotion;
            if (id != null) {
                // Update existing
                logger.info("Updating existing promotion with ID: {}", id);
                Optional<Promotion> promotionOpt = promotionRepository.findById(id);
                if (promotionOpt.isEmpty()) {
                    logger.error("Promotion not found for update, ID: {}", id);
                    throw new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi");
                }
                promotion = promotionOpt.get();
                logger.info("Found existing promotion: {}", promotion.getPromotionName());
            } else {
                // Create new
                logger.info("Creating new promotion");
                promotion = new Promotion();
                
                // Set createdBy to admin user (ID = 1)
                Optional<User> adminOpt = userRepository.findById(1);
                if (adminOpt.isPresent()) {
                    promotion.setCreatedBy(adminOpt.get());
                    logger.info("Set createdBy to admin user");
                } else {
                    logger.error("Admin user (ID=1) not found");
                    throw new IllegalArgumentException("Không tìm thấy admin user trong hệ thống");
                }
            }
            
            // Set all values with proper types
            promotion.setPromotionName(promotionName);
            promotion.setDescription(description != null ? description : "");
            promotion.setPromotionType(promotionTypeEnum);
            promotion.setDiscountType(discountTypeEnum);
            promotion.setDiscountValue(discount);
            
            // Parse BigDecimal values
            promotion.setMinOrderValue(
                (minOrderValue != null && !minOrderValue.isEmpty()) 
                    ? new BigDecimal(minOrderValue) 
                    : BigDecimal.ZERO
            );
            
            promotion.setMaxDiscountAmount(
                (maxDiscountAmount != null && !maxDiscountAmount.isEmpty()) 
                    ? new BigDecimal(maxDiscountAmount) 
                    : null
            );
            
            // Parse Integer values
            promotion.setUsageLimit(
                (usageLimit != null && !usageLimit.isEmpty()) 
                    ? Integer.parseInt(usageLimit) 
                    : null
            );
            
            promotion.setUsageLimitPerUser(Integer.parseInt(usageLimitPerUser));
            promotion.setPriority(Integer.parseInt(priority));
            promotion.setIsActive(isActive);
            
            // Parse dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            try {
                promotion.setValidFrom(LocalDateTime.parse(validFrom, formatter));
                promotion.setValidTo(LocalDateTime.parse(validTo, formatter));
            } catch (Exception e) {
                logger.error("Date parsing error: {}", e.getMessage());
                throw new IllegalArgumentException("Lỗi định dạng ngày tháng: " + e.getMessage());
            }
            
            // Save to database
            Promotion savedPromotion = promotionRepository.save(promotion);
            logger.info("Promotion saved successfully: ID={}, Name={}", savedPromotion.getId(), savedPromotion.getPromotionName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                id != null ? "Cập nhật chương trình khuyến mãi thành công!" : "Tạo chương trình khuyến mãi thành công!");
                
        } catch (NumberFormatException e) {
            logger.error("Number format error in savePromotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi định dạng số: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error in savePromotion: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error in savePromotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
    /**
     * Trang thống kê chương trình khuyến mãi
     */
    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        logger.info("=== ShowStatistics called ===");
        try {
            long totalPromotions = promotionRepository.count();
            long activePromotions = promotionRepository.countByIsActive(true);
            
            logger.info("Total promotions: {}, Active promotions: {}", totalPromotions, activePromotions);
            
            model.addAttribute("message", "Thống kê chương trình khuyến mãi");
            model.addAttribute("totalPromotions", totalPromotions);
            model.addAttribute("activePromotions", activePromotions);
            model.addAttribute("totalUsage", 156); // TODO: Calculate from usage table
            model.addAttribute("totalDiscount", "2,500,000₫"); // TODO: Calculate from usage table
        } catch (Exception e) {
            logger.error("Error in showStatistics: ", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "admin/promotion/statistics";
    }
    
    /**
     * Debug endpoint để kiểm tra trạng thái hiện tại
     */
    @GetMapping("/debug")
    @ResponseBody
    public String debugPromotions() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBUG PROMOTIONS ===\n");
        
        try {
            List<Promotion> allPromotions = promotionRepository.findAll();
            sb.append("Total promotions in DB: ").append(allPromotions.size()).append("\n\n");
            
            for (Promotion p : allPromotions) {
                sb.append("ID: ").append(p.getId())
                  .append(", Name: ").append(p.getPromotionName())
                  .append(", Type: ").append(p.getPromotionType())
                  .append(", Discount: ").append(p.getDiscountValue())
                  .append(", Active: ").append(p.getIsActive())
                  .append(", Valid From: ").append(p.getValidFrom())
                  .append(", Valid To: ").append(p.getValidTo())
                  .append("\n");
            }
        } catch (Exception e) {
            logger.error("Error in debugPromotions: ", e);
            sb.append("Error fetching promotions: ").append(e.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * Xóa chương trình khuyến mãi
     */
    @PostMapping("/delete")
    public String deletePromotion(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        logger.info("=== DeletePromotion called with ID: {} ===", id);
        try {
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);
            if (promotionOpt.isEmpty()) {
                logger.warn("Promotion not found for deletion, ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chương trình khuyến mãi để xóa!");
                return "redirect:/admin/promotions";
            }
            
            Promotion promotion = promotionOpt.get();
            logger.info("Deleting promotion: ID={}, Name={}", promotion.getId(), promotion.getPromotionName());
            
            // Delete related PromotionRule records
            if (promotion.getRules() != null && !promotion.getRules().isEmpty()) {
                logger.info("Deleting {} related PromotionRule records", promotion.getRules().size());
                promotionRuleRepository.deleteAll(promotion.getRules());
            }
            
            // Delete related PromotionUsage records
            if (promotion.getUsages() != null && !promotion.getUsages().isEmpty()) {
                logger.info("Deleting {} related PromotionUsage records", promotion.getUsages().size());
                promotionUsageRepository.deleteAll(promotion.getUsages());
            }
            
            // Now delete the promotion itself
            promotionRepository.delete(promotion);
            logger.info("Promotion deleted successfully: {}", promotion.getPromotionName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Xóa chương trình khuyến mãi '" + promotion.getPromotionName() + "' thành công!");
        } catch (Exception e) {
            logger.error("Error in deletePromotion: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
    /**
     * Toggle trạng thái chương trình khuyến mãi - POST endpoint
     */
    @PostMapping("/toggle/{id}")
    public String togglePromotionStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        logger.info("=== POST /toggle/{} endpoint called ===", id);
        return performToggle(id, redirectAttributes);
    }

    /**
     * Toggle trạng thái chương trình khuyến mãi - GET fallback
     */
    @GetMapping("/toggle/{id}")
    public String togglePromotionStatusGet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        logger.info("=== GET /toggle/{} endpoint called (fallback) ===", id);
        return performToggle(id, redirectAttributes);
    }

    /**
     * Perform the toggle action
     */
    private String performToggle(Integer id, RedirectAttributes redirectAttributes) {
        logger.info("*** PERFORMING TOGGLE FOR ID: {} ***", id);
        try {
            logger.info("Looking for promotion with ID: {}", id);
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);
            
            if (promotionOpt.isEmpty()) {
                logger.error("TOGGLE ERROR: Promotion not found for ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không tìm thấy chương trình khuyến mãi!");
                return "redirect:/admin/promotions";
            }
            
            Promotion promotion = promotionOpt.get();
            logger.info("Found promotion: ID={}, Name={}", promotion.getId(), promotion.getPromotionName());
            
            boolean oldStatus = promotion.getIsActive();
            logger.info("Current status: {}", oldStatus);
            
            promotion.setIsActive(!oldStatus);
            logger.info("New status will be: {}", !oldStatus);
            
            promotionRepository.save(promotion);
            logger.info("*** TOGGLE SUCCESS: Status changed from {} to {} ***", oldStatus, promotion.getIsActive());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Cập nhật trạng thái chương trình khuyến mãi thành công! (Từ " + 
                (oldStatus ? "Hoạt động" : "Không hoạt động") + " → " + 
                (!oldStatus ? "Hoạt động" : "Không hoạt động") + ")");
        } catch (Exception e) {
            logger.error("*** TOGGLE ERROR for ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
}