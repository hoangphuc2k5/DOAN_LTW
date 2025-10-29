package com.stackoverflow.controller.admin;

import com.stackoverflow.service.common.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Dashboard Controller - Trang chính và tổng quan
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AdminService adminService;

    /**
     * Trang dashboard admin
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Trang Quản Trị - Stack Overflow Clone");
        
        // User statistics
        model.addAttribute("totalUsers", adminService.getAllUsers().size());
        model.addAttribute("activeUsers", adminService.countActiveUsers());
        model.addAttribute("bannedUsers", adminService.countBannedUsers());
        model.addAttribute("newUsersToday", 0); // TODO: Implement
        
        // Content statistics - defaults for now
        model.addAttribute("totalQuestions", 0);
        model.addAttribute("newQuestionsToday", 0);
        model.addAttribute("totalAnswers", 0);
        model.addAttribute("newAnswersToday", 0);
        model.addAttribute("totalComments", 0);
        model.addAttribute("totalCategories", 0);
        model.addAttribute("totalTags", 0);
        model.addAttribute("pendingReports", 0);
        model.addAttribute("pendingQuestions", 0);
        model.addAttribute("unreadNotifications", 0);
        
        return "admin/dashboard";
    }

    /**
     * Trang tổng hợp tất cả tính năng admin
     */
    @GetMapping("/all-features")
    public String allFeatures(Model model) {
        model.addAttribute("pageTitle", "Tất Cả Tính Năng - Quản Trị");
        return "admin/all-features";
    }
}
