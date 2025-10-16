package com.example.demo.controller;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.ForgotPasswordService;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.entity.PasswordResetToken;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý Forgot Password
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController {
    
    private final ForgotPasswordService forgotPasswordService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Hiển thị trang quên mật khẩu
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model, HttpSession session) {
        // Nếu đã login rồi thì redirect về dashboard
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }
    
    /**
     * Xử lý yêu cầu quên mật khẩu (Web Form)
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        
        Map<String, Object> response = forgotPasswordService.sendResetPasswordOtp(request.getEmail());
        
        if ((Boolean) response.get("success")) {
            redirectAttributes.addFlashAttribute("successMessage", response.get("message"));
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("expiresInMinutes", response.get("expiresInMinutes"));
            return "redirect:/reset-password";
        } else {
            model.addAttribute("errorMessage", response.get("message"));
            return "auth/forgot-password";
        }
    }
    
    /**
     * API quên mật khẩu
     */
    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<?> forgotPasswordApi(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> response = forgotPasswordService.sendResetPasswordOtp(request.getEmail());
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", (String) response.get("message"));
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Hiển thị trang reset mật khẩu
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(Model model, HttpSession session) {
        // Nếu đã login rồi thì redirect về dashboard
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/dashboard";
        }
        // Kiểm tra có email từ flash attribute không
        String email = (String) model.asMap().get("email");
        log.info("Email from model: {}", email);
        if (email == null || email.trim().isEmpty()) {
            log.warn("No email found in model, redirecting to forgot-password");
            return "redirect:/forgot-password";
        }
        // Khởi tạo ResetPasswordRequest với email
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail(email);
        model.addAttribute("resetPasswordRequest", req);
        model.addAttribute("email", email);
        return "auth/reset-password";
    }
    
    /**
     * Xử lý reset mật khẩu với OTP (Web Form)
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute ResetPasswordRequest request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        
        log.info("Processing reset password request for email: {}", request.getEmail());
        
        // Kiểm tra email có trống không
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.warn("Email is empty in request");
            model.addAttribute("errorMessage", "Email không được để trống");
            return "redirect:/forgot-password";
        }
        
        if (result.hasErrors()) {
            log.warn("Validation errors: {}", result.getAllErrors());
            model.addAttribute("email", request.getEmail());
            return "auth/reset-password";
        }
        
        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password mismatch for email: {}", request.getEmail());
            model.addAttribute("errorMessage", "Mật khẩu và xác nhận mật khẩu không khớp");
            model.addAttribute("email", request.getEmail());
            return "auth/reset-password";
        }
        
        try {
            Map<String, Object> response = forgotPasswordService.resetPasswordWithOtp(
                request.getEmail(), 
                request.getOtpCode(), 
                request.getNewPassword()
            );
            
            log.info("Reset password response: {}", response);
            
            if ((Boolean) response.get("success")) {
                log.info("Password reset successful for email: {}", request.getEmail());
                redirectAttributes.addFlashAttribute("successMessage", response.get("message"));
                return "redirect:/login";
            } else {
                log.warn("Password reset failed for email: {}, reason: {}", request.getEmail(), response.get("message"));
                model.addAttribute("errorMessage", response.get("message"));
                model.addAttribute("email", request.getEmail());
                return "auth/reset-password";
            }
        } catch (Exception e) {
            log.error("Error processing reset password for email: {}", request.getEmail(), e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại");
            model.addAttribute("email", request.getEmail());
            return "auth/reset-password";
        }
    }
    
    /**
     * API reset mật khẩu với OTP
     */
    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPasswordApi(@Valid @RequestBody ResetPasswordRequest request) {
        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Mật khẩu và xác nhận mật khẩu không khớp");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> response = forgotPasswordService.resetPasswordWithOtp(
            request.getEmail(), 
            request.getOtpCode(), 
            request.getNewPassword()
        );
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", (String) response.get("message"));
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API gửi lại OTP
     */
    @PostMapping("/api/auth/resend-otp")
    @ResponseBody
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> response = forgotPasswordService.sendResetPasswordOtp(email);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", (String) response.get("message"));
            return ResponseEntity.badRequest().body(error);
        }
    }
}