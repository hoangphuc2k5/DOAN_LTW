package com.stackoverflow.controller.admin;

import com.stackoverflow.entity.Comment;
import com.stackoverflow.service.common.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin Comment Controller - Quản lý bình luận
 */
@Controller
@RequestMapping("/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Danh sách bình luận
     */
    @GetMapping
    public String listComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : 
                    Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments;
        
        if (search != null && !search.isEmpty()) {
            comments = commentService.searchComments(search, pageable);
            model.addAttribute("search", search);
        } else {
            comments = commentService.getAllComments(pageable);
        }
        
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comments.getTotalPages());
        model.addAttribute("totalItems", comments.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("pageTitle", "Comment Management - Admin");
        
        return "admin/comments/list";
    }

    /**
     * Chỉnh sửa bình luận
     */
    @PostMapping("/{id}/edit")
    public String editComment(
            @PathVariable Long id,
            @RequestParam String body,
            RedirectAttributes redirectAttributes) {
        
        try {
            commentService.updateComment(id, body);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Comment updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/comments";
    }

    /**
     * Xóa bình luận (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteComment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            commentService.softDeleteComment(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Comment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/comments";
    }

    /**
     * Xóa vĩnh viễn
     */
    @PostMapping("/{id}/permanent-delete")
    public String permanentDeleteComment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            commentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Comment permanently deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/comments";
    }

    /**
     * Duyệt bình luận
     */
    @PostMapping("/{id}/approve")
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
        
        return "redirect:/admin/comments";
    }

    /**
     * Từ chối bình luận
     */
    @PostMapping("/{id}/reject")
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
        
        return "redirect:/admin/comments";
    }
}

