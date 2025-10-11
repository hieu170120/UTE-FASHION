package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý Authentication
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
     * Xử lý login
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
            User user = authService.login(loginRequest);
            
            // Lưu user vào session
            session.setAttribute("currentUser", user);
            session.setAttribute("username", user.getUsername());
            
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
            return "redirect:/";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
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
     * Xử lý đăng ký
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
     * Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Đăng xuất thành công!");
        return "redirect:/login";
    }
}
