package com.stackoverflow.controller.user;

import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Report Controller - User báo cáo vi phạm
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private com.stackoverflow.service.common.UserService userService;

    /**
     * Form báo cáo
     */
    @GetMapping("/create")
    public String showReportForm(
            @RequestParam String type,
            @RequestParam Long id,
            Model model) {
        
        model.addAttribute("entityType", type);
        model.addAttribute("entityId", id);
        model.addAttribute("pageTitle", "Report Content");
        
        return "reports/create";
    }

    /**
     * Submit báo cáo
     */
    @PostMapping("/create")
    public String submitReport(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam String reason,
            @RequestParam String description,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            reportService.createReport(user, entityType, entityId, reason, description);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Report submitted successfully! Our moderators will review it soon.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    /**
     * Xem báo cáo của mình
     */
    @GetMapping("/my-reports")
    public String myReports(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // TODO: Add pagination
        model.addAttribute("pageTitle", "My Reports");
        
        return "reports/my-reports";
    }
}

