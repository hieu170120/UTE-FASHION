package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các request liên quan đến Profile
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final ProfileService profileService;
    
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    
    /**
     * Hiển thị trang profile
     */
    @GetMapping
    public String profilePage(HttpServletRequest request, Model model) {
        String username = getCurrentUsername(request);
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            ProfileDTO profile = profileService.getProfile(username);
            model.addAttribute("profile", profile);
            
            // Khởi tạo UpdateProfileDTO với giá trị hiện tại
            UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
            updateProfileDTO.setFullName(profile.getFullName());
            updateProfileDTO.setEmail(profile.getEmail());
            updateProfileDTO.setPhoneNumber(profile.getPhoneNumber());
            updateProfileDTO.setAvatarUrl(profile.getAvatarUrl());
            
            model.addAttribute("updateProfileDTO", updateProfileDTO);
            model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
            return "profile/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }
    
    /**
     * Cập nhật thông tin profile
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute UpdateProfileDTO updateProfileDTO,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        String username = getCurrentUsername(request);
        if (username == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateProfileDTO", bindingResult);
            redirectAttributes.addFlashAttribute("updateProfileDTO", updateProfileDTO);
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/profile";
        }
        
        try {
            profileService.updateProfile(username, updateProfileDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("updateProfileDTO", updateProfileDTO);
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDTO changePasswordDTO,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        String username = getCurrentUsername(request);
        if (username == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordDTO", bindingResult);
            redirectAttributes.addFlashAttribute("changePasswordDTO", changePasswordDTO);
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/profile";
        }
        
        try {
            profileService.changePassword(username, changePasswordDTO);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordDTO", changePasswordDTO);
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Lấy username hiện tại từ session
     */
    private String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("username");
        }
        return null;
    }
}
