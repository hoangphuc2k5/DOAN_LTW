package com.stackoverflow.controller.common;

import com.stackoverflow.entity.Question;
import com.stackoverflow.service.common.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private QuestionService questionService;

    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Authentication authentication,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions;
        
        // Check if user is Admin or Manager
        boolean isAdminOrManager = false;
        if (authentication != null) {
            isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                   a.getAuthority().equals("ROLE_MANAGER"));
        }
        
        if ("votes".equals(sort)) {
            // Admin/Manager see all, users see only unlocked
            questions = isAdminOrManager ? 
                questionService.getQuestionsByVotes(pageable) : 
                questionService.getPublicQuestionsByVotes(pageable);
        } else {
            // Admin/Manager see all, users see only unlocked
            questions = isAdminOrManager ? 
                questionService.getAllQuestions(pageable) : 
                questionService.getPublicQuestions(pageable);
        }
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "Top Questions - Stack Overflow Clone");
        
        return "home";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionService.searchQuestions(q, pageable);
        
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("searchQuery", q);
        model.addAttribute("pageTitle", "Search Results - Stack Overflow Clone");
        
        return "search";
    }
}

