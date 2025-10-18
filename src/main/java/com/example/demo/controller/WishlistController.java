package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // API: Thêm sản phẩm vào wishlist
    @PostMapping("/api/wishlist/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @RequestParam Integer productId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thêm vào yêu thích");
            return ResponseEntity.status(401).body(response);
        }

        try {
            wishlistService.addToWishlist(currentUser.getUserId(), productId);
            response.put("success", true);
            response.put("message", "Đã thêm vào danh sách yêu thích");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API: Xóa sản phẩm khỏi wishlist
    @DeleteMapping("/api/wishlist/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @RequestParam Integer productId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            wishlistService.removeFromWishlist(currentUser.getUserId(), productId);
            response.put("success", true);
            response.put("message", "Đã xóa khỏi danh sách yêu thích");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API: Kiểm tra sản phẩm có trong wishlist không
    @GetMapping("/api/wishlist/check/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkWishlist(
            @PathVariable Integer productId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.put("inWishlist", false);
            return ResponseEntity.ok(response);
        }

        boolean inWishlist = wishlistService.isInWishlist(currentUser.getUserId(), productId);
        response.put("inWishlist", inWishlist);
        return ResponseEntity.ok(response);
    }

    // Page: Hiển thị danh sách yêu thích
    @GetMapping("/wishlist")
    public String showWishlist(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Wishlist> wishlistItems = wishlistService.getUserWishlist(currentUser.getUserId());
        model.addAttribute("wishlistItems", wishlistItems);
        
        return "wishlist/wishlist";
    }
}
