package com.stackoverflow.controller.user;

import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * Xem profile của mình
     */
    @GetMapping
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
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
}

