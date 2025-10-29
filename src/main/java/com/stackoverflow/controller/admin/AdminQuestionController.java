package com.stackoverflow.controller.admin;

import com.stackoverflow.entity.Question;
import com.stackoverflow.service.common.QuestionService;
import com.stackoverflow.service.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Admin Question Controller - Quản lý bài viết
 */
@Controller
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    /**
     * Danh sách bài viết
     */
    @GetMapping
    public String listQuestions(
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
        Page<Question> questions;
        
        if (search != null && !search.isEmpty()) {
            questions = questionService.searchQuestions(search, pageable);
            model.addAttribute("search", search);
        } else {
            questions = questionService.getAllQuestions(pageable);
        }
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("totalItems", questions.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("pageTitle", "Question Management - Admin");
        
        return "admin/questions/list";
    }

    /**
     * Xem chi tiết bài viết
     */
    @GetMapping("/{id}")
    public String viewQuestion(@PathVariable Long id, Model model) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        model.addAttribute("question", question);
        model.addAttribute("pageTitle", "Question Details - Admin");
        
        return "admin/questions/view";
    }

    /**
     * Form chỉnh sửa bài viết
     */
    @GetMapping("/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        model.addAttribute("question", question);
        model.addAttribute("pageTitle", "Edit Question - Admin");
        
        return "admin/questions/edit";
    }

    /**
     * Xử lý cập nhật bài viết
     */
    @PostMapping("/{id}/edit")
    public String updateQuestion(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String body,
            RedirectAttributes redirectAttributes) {
        
        try {
            Question question = questionService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            
            question.setTitle(title);
            question.setBody(body);
            questionService.updateQuestion(question, null);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question updated successfully!");
            return "redirect:/admin/questions/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
            return "redirect:/admin/questions/" + id + "/edit";
        }
    }

    /**
     * Ghim bài viết
     */
    @PostMapping("/{id}/pin")
    public String pinQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.pinQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question pinned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    /**
     * Bỏ ghim bài viết
     */
    @PostMapping("/{id}/unpin")
    public String unpinQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.unpinQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question unpinned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    /**
     * Khóa bài viết
     */
    @PostMapping("/{id}/lock")
    public String lockQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.lockQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question locked successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }

    /**
     * Mở khóa bài viết
     */
    @PostMapping("/{id}/unlock")
    public String unlockQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.unlockQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question unlocked successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }

    /**
     * Duyệt bài viết
     */
    @PostMapping("/{id}/approve")
    public String approveQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.approveQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }

    /**
     * Từ chối bài viết
     */
    @PostMapping("/{id}/reject")
    public String rejectQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.rejectQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }

    /**
     * Xóa bài viết
     */
    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Question deleted successfully!");
            return "redirect:/admin/questions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
            return "redirect:/admin/questions/" + id;
        }
    }

    /**
     * Toggle close/open question
     */
    @PostMapping("/{id}/toggle-close")
    public String toggleCloseQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Question question = questionService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            
            if (question.getIsLocked()) {
                questionService.unlockQuestion(id);
                redirectAttributes.addFlashAttribute("successMessage", "Question opened successfully!");
            } else {
                questionService.lockQuestion(id);
                redirectAttributes.addFlashAttribute("successMessage", "Question closed successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }

    /**
     * Toggle pin/unpin question
     */
    @PostMapping("/{id}/toggle-pin")
    public String togglePinQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Question question = questionService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            
            if (question.getIsPinned() != null && question.getIsPinned()) {
                questionService.unpinQuestion(id);
                redirectAttributes.addFlashAttribute("successMessage", "Question unpinned successfully!");
            } else {
                questionService.pinQuestion(id);
                redirectAttributes.addFlashAttribute("successMessage", "Question pinned successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/questions/" + id;
    }
}

