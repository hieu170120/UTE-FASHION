package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private UserRepository userRepository;
    
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
        
        // ✅ REFRESH SESSION USER - Cập nhật số xu từ database
        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                User updatedUser = userRepository.findById(currentUser.getUserId()).orElse(currentUser);
                session.setAttribute("currentUser", updatedUser);
            }
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
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔵 [ProfileController] uploadAvatar - START");
        System.out.println("   Username: " + username);
        System.out.println("   Request URL: " + request.getRequestURL());
        System.out.println("═══════════════════════════════════════════════════════");
        
        try {
            // Validate file
            System.out.println("📍 [Upload] Step 1: Validating file...");
            if (file.isEmpty()) {
                System.out.println("❌ [Upload] File is empty!");
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file ảnh");
                return "redirect:/profile";
            }
            
            System.out.println("   - File name: " + file.getOriginalFilename());
            System.out.println("   - File size: " + file.getSize() + " bytes");
            System.out.println("   - Content type: " + file.getContentType());
            
            if (file.getSize() > MAX_FILE_SIZE) {
                System.out.println("❌ [Upload] File too large! Size: " + file.getSize());
                redirectAttributes.addFlashAttribute("error", "File quá lớn. Kích thước tối đa là 5MB");
                return "redirect:/profile";
            }
            
            // Validate file type
            System.out.println("📍 [Upload] Step 2: Validating file type...");
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                System.out.println("❌ [Upload] Original filename is null!");
                redirectAttributes.addFlashAttribute("error", "Tên file không hợp lệ");
                return "redirect:/profile";
            }
            
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            System.out.println("   - File extension: " + fileExtension);
            System.out.println("   - Allowed types config: " + allowedTypes);
            
            // Parse allowed types and trim spaces
            List<String> allowedExtensions = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("   - Allowed extensions after trim: " + allowedExtensions);
            
            if (!allowedExtensions.contains(fileExtension)) {
                System.out.println("❌ [Upload] File type not allowed!");
                redirectAttributes.addFlashAttribute("error", "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + allowedTypes);
                return "redirect:/profile";
            }
            System.out.println("✅ [Upload] File type is valid");
            
            // Create upload directory if not exists
            System.out.println("📍 [Upload] Step 3: Creating upload directory...");
            System.out.println("   - Upload directory config: " + uploadDir);
            
            // Use absolute path from project root
            Path uploadPath = Paths.get(new java.io.File("src/main/resources/static").getAbsolutePath(), uploadDir);
            System.out.println("   - Upload path (configured): " + uploadPath.toString());
            System.out.println("   - Upload path (absolute): " + uploadPath.toAbsolutePath().toString());
            
            if (!Files.exists(uploadPath)) {
                System.out.println("   ⚠️ Upload directory doesn't exist, creating...");
                Files.createDirectories(uploadPath);
                System.out.println("   ✅ Directory created successfully");
            } else {
                System.out.println("   ✅ Upload directory already exists");
            }
            
            // Generate unique filename
            System.out.println("📍 [Upload] Step 4: Generating unique filename...");
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            System.out.println("   - Generated filename: " + uniqueFilename);
            
            Path filePath = uploadPath.resolve(uniqueFilename);
            System.out.println("   - File path (relative): " + filePath.toString());
            System.out.println("   - File path (absolute): " + filePath.toAbsolutePath().toString());
            
            // Save file
            System.out.println("📍 [Upload] Step 5: Saving file to disk...");
            Files.copy(file.getInputStream(), filePath);
            
            // Verify file was saved
            boolean fileExists = Files.exists(filePath);
            long savedFileSize = fileExists ? Files.size(filePath) : 0;
            System.out.println("   ✅ File saved successfully");
            System.out.println("   - File exists on disk: " + fileExists);
            System.out.println("   - Saved file size: " + savedFileSize + " bytes");
            System.out.println("   - Original file size: " + file.getSize() + " bytes");
            
            // Generate URL
            System.out.println("📍 [Upload] Step 6: Generating avatar URL...");
            String contextPath = request.getContextPath();
            // Store only relative path without context-path for better portability
            String avatarUrl = "/static/" + uploadDir + "/" + uniqueFilename;
            System.out.println("   - Context path: " + contextPath);
            System.out.println("   - Stored avatar URL: " + avatarUrl);
            
            // Update user avatar
            System.out.println("📍 [Upload] Step 7: Updating user profile in database...");
            UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
            updateProfileDTO.setAvatarUrl(avatarUrl);
            System.out.println("   - UpdateProfileDTO avatar URL: " + updateProfileDTO.getAvatarUrl());
            
            System.out.println("   - Calling profileService.updateProfile()...");
            ProfileDTO updatedProfile = profileService.updateProfile(username, updateProfileDTO);
            
            System.out.println("   ✅ Profile updated successfully");
            System.out.println("   - Updated profile avatar URL: " + updatedProfile.getAvatarUrl());
            
            System.out.println("✅ [ProfileController] uploadAvatar - SUCCESS");
            System.out.println("═══════════════════════════════════════════════════════\n");
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật avatar thành công");
            
        } catch (IOException e) {
            System.out.println("❌ [Upload] IOException: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("❌ [Upload] RuntimeException: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ [Upload] Unexpected Exception: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi không xác định: " + e.getMessage());
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
