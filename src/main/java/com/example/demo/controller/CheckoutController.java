package com.example.demo.controller;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Hiển thị trang checkout
     */
    @GetMapping
    public String showCheckoutPage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy cart để hiển thị summary
        try {
            CartDTO cart = cartService.getCartByUserId(currentUser.getUserId());
            
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                return "redirect:/cart";
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("orderDTO", new OrderDTO());
            return "checkout/checkout";
            
        } catch (Exception e) {
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
        
        User currentUser = (User) session.getAttribute("currentUser");
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
            // ✅ LƯU THÔNG TIN VÀO SESSION (chưa tạo Order!)
            session.setAttribute("checkoutData", orderDTO);
            session.setAttribute("checkoutUserId", currentUser.getUserId());
            
            // TODO: Sau này sẽ redirect đến /payment/method-selection
            // Bây giờ tạm thời tạo order luôn để test
            OrderDTO createdOrder = orderService.createOrderFromCart(
                currentUser.getUserId(), 
                session.getId(), 
                orderDTO
            );
            
            // Xóa session data sau khi tạo order thành công
            session.removeAttribute("checkoutData");
            session.removeAttribute("checkoutUserId");
            
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công!");
            return "redirect:/order-success?orderId=" + createdOrder.getId();
            
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
        User currentUser = (User) session.getAttribute("currentUser");
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
