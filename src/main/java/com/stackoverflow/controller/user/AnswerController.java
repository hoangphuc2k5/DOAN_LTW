package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Answer;
import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import com.stackoverflow.entity.ImageAttachment;
import com.stackoverflow.service.common.AnswerService;
import com.stackoverflow.service.common.QuestionService;
import com.stackoverflow.service.common.UserService;
import com.stackoverflow.service.common.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageService imageService;

    @PostMapping("/question/{questionId}")
    public String postAnswer(
            @PathVariable Long questionId,
            @RequestParam String body,
            @RequestParam(required = false) MultipartFile[] images,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Question question = questionService.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            
            User author = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Answer answer = new Answer();
            answer.setBody(body);
            answer.setQuestion(question);
            answer.setAuthor(author);
            
            // Save answer first to get ID
            Answer savedAnswer = answerService.createAnswer(answer);
            
            // Handle image uploads if any
            if (images != null && images.length > 0) {
                for (MultipartFile imageFile : images) {
                    if (!imageFile.isEmpty()) {
                        try {
                            ImageAttachment image = imageService.saveAnswerImage(imageFile, savedAnswer);
                            savedAnswer.getImages().add(image);
                            System.out.println("✅ Saved image for answer ID: " + savedAnswer.getId() + " - Image ID: " + image.getId());
                        } catch (Exception imgEx) {
                            System.err.println("❌ Error saving image: " + imgEx.getMessage());
                        }
                    }
                }
                // Update answer with images
                answerService.updateAnswer(savedAnswer);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Answer posted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/questions/" + questionId;
    }

    @GetMapping("/{id}/edit")
    public String editAnswerForm(@PathVariable Long id, Model model, Authentication authentication) {
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!answer.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + answer.getQuestion().getId();
        }
        
        model.addAttribute("answer", answer);
        model.addAttribute("pageTitle", "Edit Answer - Stack Overflow Clone");
        return "answer/edit";
    }

    @PostMapping("/{id}/edit")
    public String editAnswer(
            @PathVariable Long id,
            @Valid @ModelAttribute Answer updatedAnswer,
            BindingResult result,
            Authentication authentication) {
        
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!answer.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + answer.getQuestion().getId();
        }
        
        answer.setBody(updatedAnswer.getBody());
        answerService.updateAnswer(answer);
        
        return "redirect:/questions/" + answer.getQuestion().getId();
    }

    @PostMapping("/{id}/delete")
    public String deleteAnswer(@PathVariable Long id, Authentication authentication) {
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long questionId = answer.getQuestion().getId();
        
        if (!answer.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + questionId;
        }
        
        answerService.deleteAnswer(id);
        return "redirect:/questions/" + questionId;
    }

    @PostMapping("/{id}/accept")
    public String acceptAnswer(@PathVariable Long id, Authentication authentication) {
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Only question author can accept answers
        if (!answer.getQuestion().getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + answer.getQuestion().getId();
        }
        
        answerService.acceptAnswer(answer);
        return "redirect:/questions/" + answer.getQuestion().getId();
    }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public String upvoteAnswer(@PathVariable Long id, Authentication authentication) {
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        answerService.upvoteAnswer(answer, user);
        return String.valueOf(answer.getVotes());
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public String downvoteAnswer(@PathVariable Long id, Authentication authentication) {
        Answer answer = answerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        answerService.downvoteAnswer(answer, user);
        return String.valueOf(answer.getVotes());
    }
}

