package com.stackoverflow.controller.admin;

import com.stackoverflow.service.common.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin Notification Controller - Gửi thông báo hệ thống
 */
@Controller
@RequestMapping("/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Trang gửi thông báo
     */
    @GetMapping("/send")
    public String showSendPage(Model model) {
        model.addAttribute("pageTitle", "Send System Notification - Admin");
        
        // Thống kê
        model.addAttribute("totalNotifications", notificationService.countAll());
        model.addAttribute("unreadNotifications", notificationService.countUnread());
        
        return "admin/notifications/send";
    }

    /**
     * Gửi thông báo đến tất cả người dùng
     */
    @PostMapping("/broadcast-all")
    public String broadcastToAll(
            @RequestParam String message,
            @RequestParam(defaultValue = "info") String type,
            RedirectAttributes redirectAttributes) {
        
        try {
            int count = notificationService.broadcastToAll(message, type);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Notification sent successfully to " + count + " users!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/admin/notifications/send";
    }

    /**
     * Gửi thông báo đến 1 role cụ thể
     */
    @PostMapping("/broadcast-role")
    public String broadcastToRole(
            @RequestParam String role,
            @RequestParam String message,
            @RequestParam(defaultValue = "info") String type,
            RedirectAttributes redirectAttributes) {
        
        try {
            int count = notificationService.broadcastToRole(role, message, type);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Notification sent successfully to " + count + " " + role + " users!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/admin/notifications/send";
    }

    /**
     * Danh sách thông báo
     */
    @GetMapping
    public String listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        // TODO: Implement listing
        model.addAttribute("pageTitle", "Notifications - Admin");
        return "admin/notifications/list";
    }
}

