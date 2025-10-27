package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentController
 * Xử lý payment flow cho COD và SePay QR
 * 
 * Flow COD: method-selection → confirm-cod → order created → payment pending
 * Flow SePay: method-selection → create QR → polling → order created → payment success
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private SePayService sePayService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CarrierService carrierService;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private PromotionService promotionService;

    @Value("${sepay.bank.account}")
    private String bankAccount;

    @Value("${sepay.bank.name}")
    private String bankName;
    
    /**
     * Lấy user hiện tại từ JWT authentication hoặc session
     */
    private User getCurrentUser(HttpSession session) {
        // Thử lấy từ session trước
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser != null) {
            return sessionUser;
        }
        
        // Lấy từ JWT authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        
        return null;
    }

    /**
     * Trang chọn phương thức thanh toán
     * GET /payment/method-selection
     */
    @GetMapping("/method-selection")
    public String showPaymentMethods(Model model, HttpSession session) {
        OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
        Integer userId = (Integer) session.getAttribute("checkoutUserId");
        
        if (checkoutData == null) {
            return "redirect:/cart";
        }
        
        // Lấy cart để hiển thị
        CartDTO cart = cartService.getCartByUserId(userId);
        
        // Lấy payment methods
        List<PaymentMethod> paymentMethods = paymentService.getAllPaymentMethods();
        
        // Lấy danh sách carriers
        List<CarrierDTO> carriers = carrierService.getActiveCarriers();
        
        // Lấy danh sách coupons và promotions đang active
        List<Coupon> availableCoupons = couponService.getAvailableCoupons();
        List<PromotionDTO> availablePromotions = promotionService.getAvailablePromotions();
        
        // Lấy thông tin coupon, promotion và carrier đã chọn từ session (nếu có)
        String appliedCouponCode = (String) session.getAttribute("appliedCouponCode");
        String appliedPromotionId = (String) session.getAttribute("appliedPromotionId");
        BigDecimal couponDiscount = (BigDecimal) session.getAttribute("couponDiscount");
        BigDecimal promotionDiscount = (BigDecimal) session.getAttribute("promotionDiscount");
        Integer selectedCarrierId = (Integer) session.getAttribute("selectedCarrierId");
        BigDecimal shippingFee = (BigDecimal) session.getAttribute("shippingFee");
        
        // Tính tổng tiền
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal total = subtotal;
        
        if (shippingFee != null) {
            total = total.add(shippingFee);
        }
        
        // Tính tổng discount từ cả coupon và promotion
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (couponDiscount != null) {
            totalDiscount = totalDiscount.add(couponDiscount);
        }
        if (promotionDiscount != null) {
            totalDiscount = totalDiscount.add(promotionDiscount);
        }
        
        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            total = total.subtract(totalDiscount);
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutData", checkoutData);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("carriers", carriers);
        model.addAttribute("availableCoupons", availableCoupons);
        model.addAttribute("availablePromotions", availablePromotions);
        model.addAttribute("appliedCouponCode", appliedCouponCode);
        model.addAttribute("appliedPromotionId", appliedPromotionId);
        model.addAttribute("couponDiscount", couponDiscount);
        model.addAttribute("promotionDiscount", promotionDiscount);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("selectedCarrierId", selectedCarrierId);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("finalTotal", total);
        
        return "payment/method-selection";
    }
    
    /**
     * Áp dụng mã giảm giá
     * POST /payment/apply-coupon
     */
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            Integer userId = (Integer) session.getAttribute("checkoutUserId");
            CartDTO cart = cartService.getCartByUserId(userId);
            
            // Validate coupon
            Coupon coupon = couponService.validateCoupon(couponCode, cart.getTotalAmount());
            
            // Tính discount
            BigDecimal discount = couponService.calculateDiscount(coupon, cart.getTotalAmount());
            
            // Lưu vào session
            session.setAttribute("appliedCouponCode", couponCode);
            session.setAttribute("couponDiscount", discount);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Áp dụng mã giảm giá thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/payment/method-selection";
    }
    
    /**
     * Xóa mã giảm giá
     * POST /payment/remove-coupon
     */
    @PostMapping("/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("appliedCouponCode");
        session.removeAttribute("couponDiscount");
        redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công");
        return "redirect:/payment/method-selection";
    }
    
    /**
     * Áp dụng mã khuyến mãi (Promotion)
     * POST /payment/apply-promotion
     */
    @PostMapping("/apply-promotion")
    public String applyPromotion(@RequestParam Integer promotionId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Integer userId = (Integer) session.getAttribute("checkoutUserId");
            CartDTO cart = cartService.getCartByUserId(userId);
            
            // Validate promotion
            PromotionDTO promotion = promotionService.validatePromotionForUser(promotionId, userId, cart.getTotalAmount());
            
            // Tính discount
            BigDecimal discount = promotionService.calculateDiscount(promotion, cart.getTotalAmount());
            
            // Lưu vào session
            session.setAttribute("appliedPromotionId", promotionId.toString());
            session.setAttribute("promotionDiscount", discount);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Áp dụng mã khuyến mãi thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/payment/method-selection";
    }
    
    /**
     * Xóa mã khuyến mãi
     * POST /payment/remove-promotion
     */
    @PostMapping("/remove-promotion")
    public String removePromotion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("appliedPromotionId");
        session.removeAttribute("promotionDiscount");
        redirectAttributes.addFlashAttribute("successMessage", "Xóa mã khuyến mãi thành công");
        return "redirect:/payment/method-selection";
    }
    
    /**
     * Chọn carrier (nhà vận chuyển)
     * POST /payment/select-carrier
     */
    @PostMapping("/select-carrier")
    public String selectCarrier(@RequestParam Integer carrierId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            CarrierDTO carrier = carrierService.getCarrierById(carrierId);
            
            // Lưu vào session
            session.setAttribute("selectedCarrierId", carrierId);
            session.setAttribute("shippingFee", carrier.getDefaultShippingFee());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Chọn nhà vận chuyển thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/payment/method-selection";
    }

    /**
     * COD - Tạo order ngay
     * POST /payment/confirm-cod
     */
    @PostMapping("/confirm-cod")
    public String confirmCOD(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
            Integer userId = (Integer) session.getAttribute("checkoutUserId");
            Integer selectedCarrierId = (Integer) session.getAttribute("selectedCarrierId");
            
            if (checkoutData == null) {
                return "redirect:/cart";
            }
            
            // ✅ VALIDATE: Phải chọn carrier trước
            if (selectedCarrierId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn nhà vận chuyển trước khi thanh toán!");
                return "redirect:/payment/method-selection";
            }
            
            // Set carrier vào checkoutData
            checkoutData.setCarrierId(selectedCarrierId);
            
            // ✅ LẤY VÀ SET DISCOUNT TỪ SESSION
            BigDecimal couponDiscount = (BigDecimal) session.getAttribute("couponDiscount");
            BigDecimal promotionDiscount = (BigDecimal) session.getAttribute("promotionDiscount");
            
            // Tính tổng discount
            BigDecimal totalDiscount = BigDecimal.ZERO;
            if (couponDiscount != null) {
                totalDiscount = totalDiscount.add(couponDiscount);
            }
            if (promotionDiscount != null) {
                totalDiscount = totalDiscount.add(promotionDiscount);
            }
            
            // Set discount vào checkoutData để truyền vào order
            checkoutData.setDiscountAmount(totalDiscount);
            
            // ✅ TẠO ORDER (với discount)
            OrderDTO createdOrder = orderService.createOrderFromCart(userId, session.getId(), checkoutData);
            
            // ✅ TẠO PAYMENT (COD - Pending)
            paymentService.createCODPayment(createdOrder.getId());
            
            // Xóa session
            session.removeAttribute("checkoutData");
            session.removeAttribute("checkoutUserId");
            session.removeAttribute("couponDiscount");
            session.removeAttribute("promotionDiscount");
            session.removeAttribute("appliedCouponCode");
            session.removeAttribute("appliedPromotionId");
            
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Thanh toán khi nhận hàng.");
            return "redirect:/checkout/order-success?orderId=" + createdOrder.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/method-selection";
        }
    }

    /**
     * SePay QR - Hiển thị QR Code
     * POST /payment/sepay-qr/create
     */
    @PostMapping("/sepay-qr/create")
    public String createSePayQR(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
            Integer userId = (Integer) session.getAttribute("checkoutUserId");
            Integer selectedCarrierId = (Integer) session.getAttribute("selectedCarrierId");
            
            if (checkoutData == null) {
                return "redirect:/cart";
            }
            
            // ✅ VALIDATE: Phải chọn carrier trước
            if (selectedCarrierId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn nhà vận chuyển trước khi thanh toán!");
                return "redirect:/payment/method-selection";
            }
            
            // Lấy cart để tính tổng tiền
            CartDTO cart = cartService.getCartByUserId(userId);
            
            // Lấy shipping fee và discount từ session
            BigDecimal shippingFee = (BigDecimal) session.getAttribute("shippingFee");
            BigDecimal couponDiscount = (BigDecimal) session.getAttribute("couponDiscount");
            BigDecimal promotionDiscount = (BigDecimal) session.getAttribute("promotionDiscount");
            
            // Tính tổng discount
            BigDecimal totalDiscount = BigDecimal.ZERO;
            if (couponDiscount != null) {
                totalDiscount = totalDiscount.add(couponDiscount);
            }
            if (promotionDiscount != null) {
                totalDiscount = totalDiscount.add(promotionDiscount);
            }
            
            // Tính tổng tiền = subtotal + shipping - discount
            BigDecimal amount = cart.getTotalAmount();
            if (shippingFee != null) {
                amount = amount.add(shippingFee);
            }
            if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                amount = amount.subtract(totalDiscount);
            }
            
            // ⭐ GENERATE ORDER NUMBER (chưa lưu DB)
            String orderNumber = "ORD-" + System.currentTimeMillis();
            
            // Lưu vào session
            session.setAttribute("pendingOrderNumber", orderNumber);
            session.setAttribute("pendingOrderAmount", amount);
            session.setAttribute("qrGeneratedAt", System.currentTimeMillis());
            
            // Generate QR URL
            String qrUrl = sePayService.generateQRCodeUrl(orderNumber, amount);
            
            model.addAttribute("orderNumber", orderNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("qrUrl", qrUrl);
            model.addAttribute("bankAccount", bankAccount);
            model.addAttribute("bankName", bankName);
            model.addAttribute("content", "UTEFASHION " + orderNumber.replace("-", ""));
            
            return "payment/qr-payment";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/method-selection";
        }
    }

    /**
     * API Polling - Check transaction
     * GET /payment/sepay-qr/check
     * Called by JavaScript every 3 seconds
     */
    @GetMapping("/sepay-qr/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSePayTransaction(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String orderNumber = (String) session.getAttribute("pendingOrderNumber");
            BigDecimal amount = (BigDecimal) session.getAttribute("pendingOrderAmount");
            Long startTime = (Long) session.getAttribute("qrGeneratedAt");
            
            // Nếu session đã bị xóa, có thể là order đã tạo thành công rồi
            if (orderNumber == null || startTime == null) {
                // Không trả lỗi, chỉ trả pending để client dừng polling
                response.put("status", "completed");
                response.put("message", "Payment processing completed");
                return ResponseEntity.ok(response);
            }
            
            // Check timeout (60s)
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 60000) {
                response.put("status", "timeout");
                response.put("message", "QR code expired");
                return ResponseEntity.ok(response);
            }
            
            // ⭐ POLLING: Check SePay API
            SePayTransactionDTO.Transaction transaction = sePayService.findMatchingTransaction(
                orderNumber, 
                amount, 
                startTime
            );
            
            if (transaction != null) {
                // ✅ FOUND! Tạo order
                OrderDTO checkoutData = (OrderDTO) session.getAttribute("checkoutData");
                Integer userId = (Integer) session.getAttribute("checkoutUserId");
                Integer selectedCarrierId = (Integer) session.getAttribute("selectedCarrierId");
                
                // Set carrier vào checkoutData
                if (selectedCarrierId != null) {
                    checkoutData.setCarrierId(selectedCarrierId);
                }
                
                // ✅ LẤY VÀ SET DISCOUNT TỪ SESSION
                BigDecimal couponDiscount = (BigDecimal) session.getAttribute("couponDiscount");
                BigDecimal promotionDiscount = (BigDecimal) session.getAttribute("promotionDiscount");
                
                // Tính tổng discount
                BigDecimal totalDiscount = BigDecimal.ZERO;
                if (couponDiscount != null) {
                    totalDiscount = totalDiscount.add(couponDiscount);
                }
                if (promotionDiscount != null) {
                    totalDiscount = totalDiscount.add(promotionDiscount);
                }
                
                // Set discount vào checkoutData để truyền vào order
                checkoutData.setDiscountAmount(totalDiscount);
                
                // TẠO ORDER (với discount)
                OrderDTO createdOrder = orderService.createOrderFromCart(userId, session.getId(), checkoutData);
                
                // TẠO PAYMENT (Success)
                paymentService.createSePayPayment(
                    createdOrder.getId(), 
                    transaction.getId().toString(),
                    transaction.toString()
                );
                
                // Xóa session
                session.removeAttribute("checkoutData");
                session.removeAttribute("checkoutUserId");
                session.removeAttribute("pendingOrderNumber");
                session.removeAttribute("pendingOrderAmount");
                session.removeAttribute("qrGeneratedAt");
                session.removeAttribute("couponDiscount");
                session.removeAttribute("promotionDiscount");
                session.removeAttribute("appliedCouponCode");
                session.removeAttribute("appliedPromotionId");
                
                response.put("status", "success");
                response.put("orderId", createdOrder.getId());
                response.put("transactionId", transaction.getId());
                
            } else {
                // Chưa tìm thấy
                response.put("status", "pending");
                response.put("message", "Waiting for payment...");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}