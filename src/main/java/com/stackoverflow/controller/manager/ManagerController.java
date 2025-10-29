package com.stackoverflow.controller.manager;

import com.stackoverflow.service.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manager Controller - Tất cả chức năng Manager
 */
@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerController {

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Manager Dashboard
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Manager Dashboard");
        model.addAttribute("pendingQuestions", moderationService.countPendingQuestions());
        model.addAttribute("pendingComments", moderationService.countPendingComments());
        
        return "manager/dashboard";
    }

    /**
     * 1. Duyệt bài viết
     */
    @GetMapping("/questions/pending")
    public String pendingQuestions(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        var questions = moderationService.getPendingQuestions(pageable);
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("pageTitle", "Pending Questions - Manager");
        
        return "manager/questions-pending";
    }

    @PostMapping("/questions/{id}/approve")
    public String approveQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.approveQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Question approved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/questions/pending";
    }

    @PostMapping("/questions/{id}/reject")
    public String rejectQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.rejectQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Question rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/questions/pending";
    }

    /**
     * 2. Duyệt bình luận
     */
    @GetMapping("/comments/pending")
    public String pendingComments(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        var comments = moderationService.getPendingComments(pageable);
        
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Pending Comments - Manager");
        
        return "manager/comments-pending";
    }

    @PostMapping("/comments/{id}/approve")
    public String approveComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            commentService.approveComment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Comment approved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/comments/pending";
    }

    /**
     * 6. Báo cáo thống kê
     */
    @GetMapping("/statistics")
    public String statistics(Model model) {
        var stats = statisticsService.getOverviewStatistics();
        model.addAllAttributes(stats);
        model.addAttribute("pageTitle", "Statistics - Manager");
        return "manager/statistics";
    }

    /**
     * 7. Gửi thông báo đến thành viên
     */
    @GetMapping("/notifications/send")
    public String sendNotification(Model model) {
        model.addAttribute("pageTitle", "Send Notification - Manager");
        return "manager/send-notification";
    }

    @PostMapping("/notifications/broadcast")
    public String broadcastNotification(
            @RequestParam String message,
            @RequestParam(defaultValue = "info") String type,
            RedirectAttributes redirectAttributes) {
        
        try {
            int count = notificationService.broadcastToRole("ROLE_USER", message, type);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Notification sent to " + count + " users!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/manager/notifications/send";
    }

    /**
     * 8. Ghim bài viết
     */
    @PostMapping("/questions/{id}/pin")
    public String pinQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.pinQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Question pinned!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager";
    }

    @PostMapping("/questions/{id}/unpin")
    public String unpinQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.unpinQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Question unpinned!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager";
    }

    /**
     * 4. Theo dõi thành viên
     */
    @GetMapping("/activity-logs")
    public String activityLogs(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, 50, Sort.by("createdAt").descending());
        var logs = activityLogService.getAllLogs(pageable);
        
        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Activity Logs - Manager");
        
        return "manager/activity-logs";
    }
}

