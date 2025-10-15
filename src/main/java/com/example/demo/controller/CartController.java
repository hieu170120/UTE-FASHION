package com.example.demo.controller;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.entity.User;
import com.example.demo.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        String sessionId = session.getId();
        CartDTO cart;

        try {
            if (currentUser != null) {
                cart = cartService.getCartByUserId(currentUser.getUserId());
            } else {
                cart = cartService.getCartBySessionId(sessionId);
            }
        } catch (Exception e) {
            // Nếu có lỗi, tạo cart rỗng
            cart = new CartDTO();
            cart.setCartItems(new java.util.ArrayList<>());
            cart.setTotalAmount(java.math.BigDecimal.ZERO);
        }
        
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/items")
    public String addToCart(@ModelAttribute CartItemDTO cartItemDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        String sessionId = session.getId();
        cartService.addToCart(cartItemDTO, currentUser != null ? currentUser.getUserId() : null, sessionId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        return "redirect:/cart";
    }

    @PostMapping("/items/{id}/update")
    public String updateCartItem(@PathVariable Integer id, @RequestParam Integer quantity, RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItem(id, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật số lượng!");
        }
        return "redirect:/cart";
    }

    @GetMapping("/items/{id}/delete")
    public String removeCartItem(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            cartService.removeCartItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm khỏi giỏ hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm!");
        }
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        String sessionId = session.getId();
        try {
            cartService.clearCart(currentUser != null ? currentUser.getUserId() : null, sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa toàn bộ giỏ hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa giỏ hàng!");
        }
        return "redirect:/cart";
    }

    // API endpoint để lấy mini cart data (cho dropdown)
    @GetMapping("/api/mini-cart")
    @ResponseBody
    public CartDTO getMiniCart(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        String sessionId = session.getId();
        
        try {
            if (currentUser != null) {
                return cartService.getCartByUserId(currentUser.getUserId());
            } else {
                return cartService.getCartBySessionId(sessionId);
            }
        } catch (Exception e) {
            return new CartDTO(); // Trả về cart rỗng nếu có lỗi
        }
    }
}
