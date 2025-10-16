package com.example.demo.controller;

import com.example.demo.service.AuthService;
import com.example.demo.service.EmailService;
import com.example.demo.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý xác thực email OTP
 */
@Controller
@RequiredArgsConstructor
public class EmailVerificationController {
    
    private final AuthService authService;
    private final EmailService emailService;
    
    /**
     * Hiển thị trang xác thực email
     */
    @GetMapping("/verify-email")
    public String showVerifyEmailPage(@RequestParam String email, Model model, HttpSession session) {
        System.out.println("=== DEBUG VERIFY EMAIL PAGE ===");
        System.out.println("Requested email: " + email);
        System.out.println("Session ID: " + session.getId());
        
        // Kiểm tra email có trong session không
        String sessionEmail = (String) session.getAttribute("pendingEmail");
        System.out.println("Session email: " + sessionEmail);
        
        // Debug tất cả session attributes
        System.out.println("All session attributes:");
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attrName = attributeNames.nextElement();
            Object attrValue = session.getAttribute(attrName);
            System.out.println("  " + attrName + " = " + attrValue);
        }
        
        if (sessionEmail == null || !sessionEmail.equals(email)) {
            System.out.println("❌ Session email mismatch or null - redirecting to register");
            System.out.println("Session email: " + sessionEmail);
            System.out.println("Request email: " + email);
            return "redirect:/register";
        }
        
        System.out.println("✅ Session email matches - showing verify page");
        model.addAttribute("email", email);
        return "auth/verify-email";
    }
    
    /**
     * Xử lý xác thực OTP
     */
    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String email,
                            @RequestParam String otpCode,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        
        try {
            User verifiedUser = authService.verifyEmail(email, otpCode);
            
            // Xóa email khỏi session
            session.removeAttribute("pendingEmail");
            
            System.out.println("=== DEBUG VERIFICATION SUCCESS ===");
            System.out.println("User created: " + verifiedUser.getUsername());
            System.out.println("Email verified: " + verifiedUser.getIsEmailVerified());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Xác thực email thành công! Tài khoản của bạn đã được kích hoạt.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "auth/verify-email";
        }
    }
    
    /**
     * Gửi lại mã OTP
     */
    @PostMapping("/resend-otp")
    @ResponseBody
    public Map<String, Object> resendOTP(@RequestParam String email, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra email có trong session không
            String sessionEmail = (String) session.getAttribute("pendingEmail");
            if (sessionEmail == null || !sessionEmail.equals(email)) {
                response.put("success", false);
                response.put("message", "Email không hợp lệ");
                return response;
            }
            
            emailService.sendOTP(email);
            response.put("success", true);
            response.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * API endpoint để xác thực OTP
     */
    @PostMapping("/api/auth/verify-email")
    @ResponseBody
    public Map<String, Object> verifyEmailApi(@RequestParam String email,
                                            @RequestParam String otpCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User verifiedUser = authService.verifyEmail(email, otpCode);
            
            response.put("success", true);
            response.put("message", "Xác thực email thành công!");
            response.put("userId", verifiedUser.getUserId());
            response.put("username", verifiedUser.getUsername());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * API endpoint để gửi lại OTP cho email verification
     */
    @PostMapping("/api/auth/resend-verification-otp")
    @ResponseBody
    public Map<String, Object> resendOTPApi(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            emailService.sendOTP(email);
            response.put("success", true);
            response.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}
