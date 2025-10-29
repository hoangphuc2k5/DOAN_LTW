package com.stackoverflow.controller.admin;

import com.stackoverflow.entity.SystemSettings;
import com.stackoverflow.service.common.SystemSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin System Controller - Quản trị hệ thống
 */
@Controller
@RequestMapping("/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    @Autowired
    private SystemSettingsService systemSettingsService;

    /**
     * Cài đặt hệ thống
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        SystemSettings settings = systemSettingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("pageTitle", "System Settings - Admin");
        return "admin/system/settings";
    }

    /**
     * Cập nhật cài đặt
     */
    @PostMapping("/settings/update")
    public String updateSettings(
            @ModelAttribute SystemSettings settings,
            RedirectAttributes redirectAttributes) {
        
        try {
            systemSettingsService.updateSettings(settings);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/admin/system/settings";
    }

    /**
     * Quản lý database - placeholder
     */
    @GetMapping("/database")
    public String database(Model model) {
        model.addAttribute("pageTitle", "Database Management - Admin");
        return "admin/system/database";
    }

    /**
     * Phân quyền - placeholder
     */
    @GetMapping("/permissions")
    public String permissions(Model model) {
        model.addAttribute("pageTitle", "Permissions Management - Admin");
        return "admin/system/permissions";
    }

    /**
     * Quản lý vai trò - placeholder
     */
    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("pageTitle", "Roles Management - Admin");
        return "admin/system/roles";
    }

    /**
     * Upload logo
     */
    @PostMapping("/upload-logo")
    public String uploadLogo(
            @RequestParam("logoFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            String logoPath = systemSettingsService.uploadLogo(file);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Đã tải lên logo thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/system/settings";
    }

    /**
     * Upload favicon
     */
    @PostMapping("/upload-favicon")
    public String uploadFavicon(
            @RequestParam("faviconFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            String faviconPath = systemSettingsService.uploadFavicon(file);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Đã tải lên favicon thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/system/settings";
    }

    /**
     * Update theme colors
     */
    @PostMapping("/update-theme")
    public String updateTheme(
            @RequestParam String primaryColor,
            @RequestParam String secondaryColor,
            @RequestParam String accentColor,
            RedirectAttributes redirectAttributes) {
        
        try {
            systemSettingsService.updateThemeColors(primaryColor, secondaryColor, accentColor);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Đã cập nhật màu giao diện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/system/settings";
    }
}

