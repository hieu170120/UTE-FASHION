package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.security.JwtUtil;
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

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository =
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository();

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== DASHBOARD CONTROLLER CALLED ===");
        
        User currentUser = (User) session.getAttribute("currentUser");
        System.out.println("Current user from session: " + (currentUser != null ? currentUser.getUsername() : "NULL"));
        
        // Kiểm tra đã đăng nhập chưa
        if (currentUser == null) {
            System.out.println("No user in session, redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("Adding user to model: " + currentUser.getUsername());
        model.addAttribute("currentUser", currentUser);
        
        // Hiển thị JWT token từ session
        String jwtToken = (String) session.getAttribute("jwtToken");
        if (jwtToken != null) {
            System.out.println("=== JWT TOKEN FROM SESSION ===");
            System.out.println("Token: " + jwtToken);
            System.out.println("Token length: " + jwtToken.length());
            model.addAttribute("jwtToken", jwtToken);
        } else {
            System.out.println("No JWT token found in session");
        }
        
        System.out.println("Returning dashboard template");
        return "dashboard";
    }
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest,
                       BindingResult result,
                       HttpSession session,
                       jakarta.servlet.http.HttpServletRequest request,
                       jakarta.servlet.http.HttpServletResponse response,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }
        
        try {
            User user = authService.loginUser(loginRequest);
            String token = jwtUtil.generateToken(user.getUsername());
            session.setAttribute("currentUser", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("jwtToken", token);
            
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPasswordHash())
                    .authorities(user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                        .toArray(org.springframework.security.core.GrantedAuthority[]::new))
                    .build();
            
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            var securityContext = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, request, response);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
            return "redirect:/"; // Chuyển hướng về trang chủ mới
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }
    
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
    
    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model,
                          HttpSession session) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            var pendingUser = authService.register(registerRequest);
            
            // Lưu email vào session để xác thực
            System.out.println("=== DEBUG REGISTER SUCCESS ===");
            System.out.println("Registering email: " + registerRequest.getEmail());
            System.out.println("Session ID: " + session.getId());
            System.out.println("Pending User ID: " + pendingUser.getPendingId());
            session.setAttribute("pendingEmail", registerRequest.getEmail());
            System.out.println("✅ Session pendingEmail set to: " + registerRequest.getEmail());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return "redirect:/verify-email?email=" + registerRequest.getEmail();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
    
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerApi(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            var pendingUser = authService.register(registerRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            response.put("pendingId", pendingUser.getPendingId());
            response.put("username", pendingUser.getUsername());
            response.put("email", pendingUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Đăng xuất thành công!");
        return "redirect:/login";
    }
    
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logoutApi() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công! Vui lòng xóa token ở phía client.");
        return ResponseEntity.ok(response);
    }
}
