package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.AnswerService;
import com.stackoverflow.service.common.QuestionService;
import com.stackoverflow.service.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "36") int size,
            @RequestParam(defaultValue = "reputation") String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        
        if (search != null && !search.isEmpty()) {
            users = userService.searchUsers(search, pageable);
            model.addAttribute("searchQuery", search);
        } else if ("reputation".equals(sort)) {
            users = userService.getUsersByReputation(pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "Users - Stack Overflow Clone");
        
        return "users/list";
    }

    @GetMapping("/{id}")
    public String viewUserProfile(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "questions") String tab,
            Model model) {
        
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Increment profile views
        userService.incrementViews(user);
        
        Pageable pageable = PageRequest.of(page, size);
        
        if ("answers".equals(tab)) {
            model.addAttribute("answers", answerService.getAnswersByAuthor(user, pageable));
        } else {
            Page<Question> questions = questionService.getQuestionsByAuthor(user, pageable);
            model.addAttribute("questions", questions);
        }
        
        Long questionCount = questionService.countByAuthor(user);
        Long answerCount = answerService.countByAuthor(user);
        
        model.addAttribute("user", user);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("answerCount", answerCount);
        model.addAttribute("currentTab", tab);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", user.getUsername() + " - User Profile");
        
        return "users/profile";
    }

    @GetMapping("/{id}/edit")
    public String editProfileForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getId().equals(currentUser.getId())) {
            return "redirect:/users/" + id;
        }
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile - Stack Overflow Clone");
        return "users/edit";
    }

    @PostMapping("/{id}/edit")
    public String editProfile(
            @PathVariable Long id,
            @ModelAttribute User updatedUser,
            Authentication authentication) {
        
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getId().equals(currentUser.getId())) {
            return "redirect:/users/" + id;
        }
        
        user.setAbout(updatedUser.getAbout());
        user.setLocation(updatedUser.getLocation());
        user.setWebsite(updatedUser.getWebsite());
        userService.updateUser(user);
        
        return "redirect:/users/" + id;
    }
}

