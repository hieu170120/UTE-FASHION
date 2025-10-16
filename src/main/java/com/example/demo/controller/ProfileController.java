package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý các request liên quan đến Profile
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final ProfileService profileService;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Value("${app.upload.allowed-types}")
    private String allowedTypes;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
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
     * Upload avatar
     */
    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        String username = getCurrentUsername(request);
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file ảnh");
                return "redirect:/profile";
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "File quá lớn. Kích thước tối đa là 5MB");
                return "redirect:/profile";
            }
            
            // Validate file type
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                redirectAttributes.addFlashAttribute("error", "Tên file không hợp lệ");
                return "redirect:/profile";
            }
            
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
            
            if (!allowedExtensions.contains(fileExtension)) {
                redirectAttributes.addFlashAttribute("error", "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + allowedTypes);
                return "redirect:/profile";
            }
            
            // Create upload directory if not exists
            Path uploadPath = Paths.get("src/main/resources/static/" + uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath);
            
            // Generate URL - sử dụng đường dẫn tương đối với context path
            String avatarUrl = "/UTE_Fashion/static/" + uploadDir + "/" + uniqueFilename;
            
            // Update user avatar
            UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
            updateProfileDTO.setAvatarUrl(avatarUrl);
            
            System.out.println("DEBUG: Uploading avatar for user: " + username);
            System.out.println("DEBUG: Generated avatar URL: " + avatarUrl);
            System.out.println("DEBUG: File saved to: " + filePath.toString());
            System.out.println("DEBUG: updateProfileDTO.getAvatarUrl() before service call: " + updateProfileDTO.getAvatarUrl());
            
            ProfileDTO updatedProfile = profileService.updateProfile(username, updateProfileDTO);
            
            System.out.println("DEBUG: Profile updated successfully. New avatar URL: " + updatedProfile.getAvatarUrl());
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật avatar thành công");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload file: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
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
    
    /**
     * Test endpoint để kiểm tra avatar URL trong database
     */
    @GetMapping("/test-avatar")
    @ResponseBody
    public String testAvatar(HttpServletRequest request) {
        String username = getCurrentUsername(request);
        if (username == null) {
            return "Not logged in";
        }
        
        try {
            ProfileDTO profile = profileService.getProfile(username);
            return "Username: " + username + "\nAvatar URL: " + profile.getAvatarUrl();
        } catch (RuntimeException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Lấy file extension từ filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
