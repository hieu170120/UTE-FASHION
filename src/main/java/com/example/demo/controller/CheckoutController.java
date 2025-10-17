package com.example.demo.controller;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CarrierDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.CarrierService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CheckoutController - Xử lý checkout flow
 * Theo flow: Cart → Checkout → Session → (Payment) → Order
 * Author: Team UTE Fashion
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CarrierService carrierService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy user hiện tại từ JWT authentication hoặc session
     */
    private User getCurrentUser(HttpSession session) {
        // Thử lấy từ session trước (cho login bằng form)
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
     * Hiển thị trang checkout
     * - Nếu chưa đăng nhập: hiển thị giao diện yêu cầu đăng nhập/đăng ký
     * - Nếu đã đăng nhập: hiển thị form checkout với thông tin user và cart
     */
    @GetMapping
    public String showCheckoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        
        // Nếu chưa đăng nhập -> hiển thị giao diện yêu cầu login
        if (currentUser == null) {
            return "checkout/login-required";
        }

        // Đã đăng nhập -> lấy cart từ database
        try {
            CartDTO cart = cartService.getCartByUserId(currentUser.getUserId());
            
            // Kiểm tra cart có sản phẩm không
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống");
                return "redirect:/cart";
            }
            
            // Tạo OrderDTO với thông tin mặc định từ user
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setRecipientName(currentUser.getFullName());
            orderDTO.setPhoneNumber(currentUser.getPhoneNumber());
            orderDTO.setEmail(currentUser.getEmail());
            
            // Lấy danh sách carriers để khách hàng chọn
            model.addAttribute("carriers", carrierService.getActiveCarriers());
            model.addAttribute("cart", cart);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("orderDTO", orderDTO);
            
            return "checkout/checkout";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải giỏ hàng: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Xử lý checkout - Lưu vào session
     * ⚠️ QUAN TRỌNG: Không tạo Order ở đây!
     * Chỉ lưu thông tin vào session để dùng sau
     */
    @PostMapping
    public String processCheckout(@Valid @ModelAttribute OrderDTO orderDTO,
                                  BindingResult result,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            try {
                CartDTO cart = cartService.getCartByUserId(currentUser.getUserId());
                model.addAttribute("cart", cart);
            } catch (Exception e) {
                // Ignore
            }
            return "checkout/checkout";
        }

        try {
            // Lấy cart từ database
            CartDTO cart = cartService.getCartByUserId(currentUser.getUserId());
            
            // Kiểm tra cart có sản phẩm không
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống");
                return "redirect:/cart";
            }
            
            // ✅ LƯU THÔNG TIN VÀO SESSION (chưa tạo Order!)
            session.setAttribute("checkoutData", orderDTO);
            session.setAttribute("checkoutUserId", currentUser.getUserId());
            session.setAttribute("cart", cart); // Lưu cart cho payment
            
            // ✅ REDIRECT ĐẾN PAYMENT SELECTION
            // Order sẽ được tạo sau khi chọn payment method
            return "redirect:/payment/method-selection";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Trang order success
     */
    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Integer orderId, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            
            // Kiểm tra quyền xem order
            if (!order.getUserId().equals(currentUser.getUserId())) {
                return "redirect:/orders";
            }
            
            model.addAttribute("order", order);
            return "order/success";
            
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }
}
