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
import java.util.*;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<?> addToCart(@ModelAttribute CartItemDTO cartItemDTO, 
                           @RequestParam(value = "buyNow", required = false) String buyNow,
                           HttpSession session, 
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            String sessionId = session.getId();
            cartService.addToCart(cartItemDTO, currentUser != null ? currentUser.getUserId() : null, sessionId);
            
            // Nếu là "Mua ngay" thì redirect đến checkout
            if ("true".equals(buyNow)) {
                // Trả về redirect URL cho frontend
                Map<String, String> response = new HashMap<>();
                response.put("redirect", "/checkout");
                return ResponseEntity.ok(response);
            }
            
            // Ngược lại trả về success cho AJAX request
            return ResponseEntity.ok(Map.of("message", "Đã thêm sản phẩm vào giỏ hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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

    @PostMapping(value = "/batch-update", produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> batchUpdateCartItems(@RequestBody(required = false) List<Map<String, Integer>> updates) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("=== BATCH UPDATE CALLED ===");
        System.out.println("Updates: " + updates);
        try {
            if (updates == null || updates.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không có sản phẩm nào để cập nhật");
                return response;
            }
            
            int count = 0;
            for (Map<String, Integer> update : updates) {
                Integer itemId = update.get("cartItemId");
                Integer quantity = update.get("quantity");
                if (itemId != null && quantity != null && quantity > 0) {
                    cartService.updateCartItem(itemId, quantity);
                    count++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Cập nhật " + count + " sản phẩm thành công!");
            response.put("updatedCount", count);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
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
