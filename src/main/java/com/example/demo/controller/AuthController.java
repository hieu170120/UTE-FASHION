package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý Authentication với JWT
 */
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Trang chủ
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        return "index";
    }
    
    /**
     * Dashboard - trang sau khi đăng nhập
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        // Kiểm tra đã đăng nhập chưa
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("currentUser", currentUser);
        return "dashboard";
    }
    
    /**
     * Hiển thị trang login
     */
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        // Nếu đã login rồi thì redirect về home
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }
    
    /**
     * Xử lý login (cho web form - sử dụng session)
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest,
                       BindingResult result,
                       HttpSession session,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }
        
        try {
            User user = authService.loginUser(loginRequest);
            
            // Lưu user vào session
            session.setAttribute("currentUser", user);
            session.setAttribute("username", user.getUsername());
            
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }
    
    /**
     * API đăng nhập - trả về JWT token
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Hiển thị trang đăng ký
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        // Nếu đã login rồi thì redirect về home
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }
    
    /**
     * Xử lý đăng ký (cho web form)
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
    
    /**
     * API đăng ký
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerApi(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(registerRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký thành công!");
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Đăng xuất (cho web form)
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Đăng xuất thành công!");
        return "redirect:/login";
    }
    
    /**
     * API đăng xuất
     * Note: Với JWT stateless, client chỉ cần xóa token ở phía client
     */
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logoutApi() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công! Vui lòng xóa token ở phía client.");
        return ResponseEntity.ok(response);
    }
}
