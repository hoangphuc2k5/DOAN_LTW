package com.stackoverflow.controller.admin;

import com.stackoverflow.service.common.ModerationService;

import com.stackoverflow.service.common.QuestionService;
import com.stackoverflow.service.common.CommentService;
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
 * Admin Moderation Controller - Kiểm duyệt nội dung
 */
@Controller
@RequestMapping("/admin/moderation")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    /**
     * Trang kiểm duyệt chính
     */
    @GetMapping
    public String moderationDashboard(Model model) {
        model.addAttribute("pageTitle", "Content Moderation - Admin");
        
        // Statistics
        model.addAttribute("pendingQuestions", moderationService.countPendingQuestions());
        model.addAttribute("approvedQuestions", moderationService.countApprovedQuestions());
        model.addAttribute("pendingComments", moderationService.countPendingComments());
        model.addAttribute("approvedComments", moderationService.countApprovedComments());
        
        return "admin/moderation/dashboard";
    }

    /**
     * Danh sách bài viết chờ duyệt
     */
    @GetMapping("/questions/pending")
    public String pendingQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var questions = moderationService.getPendingQuestions(pageable);
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("pageTitle", "Pending Questions - Admin");
        
        return "admin/moderation/pending-questions";
    }

    /**
     * Danh sách bình luận chờ duyệt
     */
    @GetMapping("/comments/pending")
    public String pendingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var comments = moderationService.getPendingComments(pageable);
        
        model.addAttribute("pendingComments", comments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comments.getTotalPages());
        model.addAttribute("pageTitle", "Pending Comments - Admin");
        
        return "admin/moderation/pending-comments";
    }

    /**
     * Duyệt bài viết
     */
    @PostMapping("/questions/{id}/approve")
    public String approveQuestion(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            questionService.approveQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question approved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/moderation/questions/pending";
    }

    /**
     * Từ chối bài viết
     */
    @PostMapping("/questions/{id}/reject")
    public String rejectQuestion(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            questionService.rejectQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/moderation/questions/pending";
    }

    /**
     * Duyệt bình luận
     */
    @PostMapping("/comments/{id}/approve")
    public String approveComment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            commentService.approveComment(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Comment approved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/moderation/comments/pending";
    }

    /**
     * Từ chối bình luận
     */
    @PostMapping("/comments/{id}/reject")
    public String rejectComment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            commentService.rejectComment(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Comment rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/moderation/comments/pending";
    }
}

