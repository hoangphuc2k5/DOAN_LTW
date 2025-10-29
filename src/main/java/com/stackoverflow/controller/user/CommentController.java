package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Comment;

import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.CommentService;
import com.stackoverflow.service.common.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/questions/{id}/comments")
    public String addCommentToQuestion(
            @PathVariable Long id,
            @RequestParam String body,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Question question = questionService.getQuestionById(id);
            Comment comment = new Comment();
            comment.setBody(body);
            comment.setAuthor(currentUser);
            comment.setQuestion(question);
            commentService.saveComment(comment);
            redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding comment: " + e.getMessage());
        }
        
        return "redirect:/questions/" + id;
    }
}