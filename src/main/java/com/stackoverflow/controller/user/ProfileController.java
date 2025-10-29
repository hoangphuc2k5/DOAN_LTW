package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Profile Controller - Quản lý hồ sơ cá nhân
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AnswerService answerService;
    
    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;

    /**
     * Xem profile của mình
     */
    @GetMapping
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's data for profile display
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        
        Page<Question> questions = questionService.getQuestionsByAuthor(user, pageable);
        Page<com.stackoverflow.entity.Answer> answers = answerService.getAnswersByAuthor(user, pageable);
        
        // Counts
        Long totalQuestions = questionService.countByAuthor(user);
        Long totalAnswers = answerService.countByAuthor(user);
        Long totalComments = 0L; // Comments removed
        
        // Add to model
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalAnswers", totalAnswers);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("activities", List.of()); // Empty for now
        model.addAttribute("badges", List.of()); // Empty for now
        model.addAttribute("pageTitle", "My Profile");
        
        return "profile/view";
    }

    /**
     * Form chỉnh sửa profile
     */
    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");
        
        return "profile/edit";
    }

    /**
     * Cập nhật profile
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String githubUrl,
            @RequestParam(required = false) String linkedinUrl,
            @RequestParam(required = false) MultipartFile profilePicture,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setEmail(email);
            user.setBio(bio);
            user.setLocation(location);
            user.setWebsite(website);
            user.setGithubUrl(githubUrl);
            user.setLinkedinUrl(linkedinUrl);
            
            // Handle profile picture upload
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    // Validate image
                    String contentType = profilePicture.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        throw new IllegalArgumentException("File must be an image");
                    }
                    if (profilePicture.getSize() > 5 * 1024 * 1024) {
                        throw new IllegalArgumentException("File size must be less than 5MB");
                    }
                    
                    // Create avatars directory if it doesn't exist
                    Path avatarsDir = Paths.get(uploadPath, "avatars");
                    if (!Files.exists(avatarsDir)) {
                        Files.createDirectories(avatarsDir);
                    }
                    
                    // Delete old profile image if exists
                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        Path oldImagePath = avatarsDir.resolve(user.getProfileImage());
                        Files.deleteIfExists(oldImagePath);
                    }
                    
                    // Generate unique filename with userId + timestamp + UUID
                    // Format: user{id}_{yyyyMMdd_HHmmss}_{uuid}.{ext}
                    // Example: user123_20250129_143520_a1b2c3d4.jpg
                    String originalFilename = profilePicture.getOriginalFilename();
                    String extension = originalFilename != null && originalFilename.contains(".") ? 
                        originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                    
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String uuid = UUID.randomUUID().toString().substring(0, 8); // Short UUID
                    String filename = String.format("user%d_%s_%s%s", 
                        user.getId(), timestamp, uuid, extension);
                    
                    // Double check: if file exists (extremely rare), append counter
                    Path filePath = avatarsDir.resolve(filename);
                    int counter = 1;
                    while (Files.exists(filePath)) {
                        filename = String.format("user%d_%s_%s_%d%s", 
                            user.getId(), timestamp, uuid, counter, extension);
                        filePath = avatarsDir.resolve(filename);
                        counter++;
                    }
                    
                    // Save new profile image
                    Files.copy(profilePicture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    user.setProfileImage(filename);
                    
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "❌ Error uploading profile picture: " + e.getMessage());
                    return "redirect:/profile/edit";
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "❌ " + e.getMessage());
                    return "redirect:/profile/edit";
                }
            }
            
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Profile updated successfully!");
            
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
    
    /**
     * Xóa ảnh đại diện
     */
    @PostMapping("/remove-avatar")
    public String removeAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Delete old profile image if exists
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Path avatarsDir = Paths.get(uploadPath, "avatars");
                Path imagePath = avatarsDir.resolve(user.getProfileImage());
                Files.deleteIfExists(imagePath);
                
                user.setProfileImage(null);
                userService.updateUser(user);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "✅ Profile picture removed successfully!");
            }
            
            return "redirect:/profile/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Form đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("pageTitle", "Change Password");
        return "profile/change-password";
    }

    /**
     * Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "❌ Current password is incorrect!");
                return "redirect:/profile/change-password";
            }
            
            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "❌ New passwords do not match!");
                return "redirect:/profile/change-password";
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Password changed successfully!");
            
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    /**
     * Xem profile người khác
     */
    @GetMapping("/{username}")
    public String viewUserProfile(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", username + "'s Profile");
        
        return "profile/public-view";
    }
    
    /**
     * My Questions - Hiển thị tất cả câu hỏi của user (kể cả pending)
     */
    @GetMapping("/my-questions")
    public String myQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "all") String filter,
            Principal principal,
            Model model) {
        
        if (principal == null) {
            return "redirect:/login";
        }
        
        User currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Question> questions;
        
        switch (filter) {
            case "pending":
                // Chỉ hiện câu hỏi chưa duyệt
                questions = questionService.getPendingQuestionsByAuthor(currentUser, pageable);
                break;
            case "approved":
                // Chỉ hiện câu hỏi đã duyệt
                questions = questionService.getApprovedQuestionsByAuthor(currentUser, pageable);
                break;
            default:
                // Hiện tất cả
                questions = questionService.getQuestionsByAuthor(currentUser, pageable);
                break;
        }
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("filter", filter);
        model.addAttribute("pageTitle", "My Questions - Stack Overflow Clone");
        
        return "profile/my-questions";
    }
}

