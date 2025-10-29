package com.stackoverflow.controller.user;

import com.stackoverflow.entity.*;
import com.stackoverflow.service.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private ImageService imageService;

    @Autowired
    private TagService tagService;

    @GetMapping("/{id}")
    public String viewQuestion(@PathVariable Long id, Model model, Authentication authentication) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Check if question is approved or user has permission to view
        if (!question.getIsApproved()) {
            // Only author, admin, or manager can view unapproved questions
            if (authentication == null) {
                throw new RuntimeException("Question is pending moderation");
            }
            
            String username = authentication.getName();
            boolean isAuthor = question.getAuthor().getUsername().equals(username);
            boolean isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
            
            if (!isAuthor && !isAdminOrManager) {
                throw new RuntimeException("Question is pending moderation");
            }
        }
        
        // Increment views
        questionService.incrementViews(question);
        
        List<Answer> answers = answerService.getAnswersByQuestion(question);
        
        // Debug: Log images for each answer
        System.out.println("=== Question ID: " + id + " ===");
        System.out.println("Total Answers: " + answers.size());
        for (Answer answer : answers) {
            System.out.println("Answer ID: " + answer.getId() + " - Images count: " + 
                (answer.getImages() != null ? answer.getImages().size() : "NULL"));
            if (answer.getImages() != null && !answer.getImages().isEmpty()) {
                answer.getImages().forEach(img -> 
                    System.out.println("  - Image ID: " + img.getId() + ", Path: " + img.getPath())
                );
            }
        }
        
        model.addAttribute("question", question);
        model.addAttribute("answers", answers);
        model.addAttribute("questionComments", commentService.getCommentsByQuestion(question));
        model.addAttribute("pageTitle", question.getTitle() + " - Stack Overflow Clone");
        
        return "question/view";
    }

    @GetMapping("/ask")
    public String askQuestionForm(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("pageTitle", "Ask a Question - Stack Overflow Clone");
        return "question/ask";
    }

    @PostMapping("/ask")
    public String askQuestion(
            @Valid @ModelAttribute("question") Question question,
            BindingResult result,
            @RequestParam(required = false) MultipartFile[] files,
            Authentication authentication,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Ask a Question - Stack Overflow Clone");
            return "question/ask";
        }
        
        try {
            User author = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            question.setAuthor(author);
            
            // Process tags from the tag string
            if (question.getTagString() != null && !question.getTagString().isEmpty()) {
                Set<String> tagNames = Arrays.stream(question.getTagString().split("\\s*,\\s*"))
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
                Set<Tag> tags = tagService.getOrCreateTags(tagNames);
                question.setTags(tags);
            }
            
            // Save question first to get ID
            Question savedQuestion = questionService.save(question);
            
            // Handle image uploads if any
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        ImageAttachment image = imageService.saveQuestionImage(file, savedQuestion);
                        savedQuestion.getImages().add(image);
                    }
                }
                questionService.save(savedQuestion); // Save again with images
            }
            
            return "redirect:/questions/" + savedQuestion.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create question: " + e.getMessage());
            model.addAttribute("pageTitle", "Ask a Question - Stack Overflow Clone");
            return "question/ask";
        }
    }

    @GetMapping("/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model, Authentication authentication) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!question.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + id;
        }
        
        model.addAttribute("question", question);
        model.addAttribute("pageTitle", "Edit Question - Stack Overflow Clone");
        return "question/edit";
    }

    @PostMapping("/{id}/edit")
    public String editQuestion(
            @PathVariable Long id,
            @Valid @ModelAttribute Question updatedQuestion,
            @RequestParam(required = false) String tags,
            BindingResult result,
            Authentication authentication,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Question - Stack Overflow Clone");
            return "question/edit";
        }
        
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!question.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + id;
        }
        
        question.setTitle(updatedQuestion.getTitle());
        question.setBody(updatedQuestion.getBody());
        
        if (tags != null && !tags.isEmpty()) {
            Set<String> tagNames = Set.of(tags.split("\\s+"));
            questionService.updateQuestion(question, tagNames);
        } else {
            questionService.updateQuestion(question, null);
        }
        
        return "redirect:/questions/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, Authentication authentication) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!question.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/questions/" + id;
        }
        
        questionService.deleteQuestion(id);
        return "redirect:/";
    }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public String upvoteQuestion(@PathVariable Long id, Authentication authentication) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        questionService.upvoteQuestion(question, user);
        return String.valueOf(question.getVotes());
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public String downvoteQuestion(@PathVariable Long id, Authentication authentication) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        questionService.downvoteQuestion(question, user);
        return String.valueOf(question.getVotes());
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam String tags,
            @AuthenticationPrincipal User currentUser) {
        try {
            // Create question
            Question question = new Question();
            question.setTitle(title);
            question.setBody(body);
            question.setAuthor(currentUser);

            // Handle group posting
            if (groupId != null) {
                UserGroup group = groupService.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
                
                if (!group.getMembers().contains(currentUser)) {
                    return ResponseEntity.badRequest()
                        .body("You must be a member of the group to post");
                }
                
                question.setGroup(group);
            }

            // Process tags
            Set<Tag> tagSet = processTags(tags);
            question.setTags(tagSet);

            // Save question first to get ID
            Question savedQuestion = questionService.save(question);

            // Handle image uploads if any
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    imageService.saveImage(image, savedQuestion.getId(), null, currentUser);
                }
            }

            // Send notifications
            if (groupId != null) {
                webSocketService.notifyNewPost(question.getGroup(), savedQuestion);
            }

            return ResponseEntity.ok(savedQuestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to create question: " + e.getMessage());
        }
    }

    private Set<Tag> processTags(String tagString) {
        return Arrays.stream(tagString.split("\\s+"))
            .map(tagName -> {
                Tag tag = tagService.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagService.save(newTag);
                    });
                return tag;
            })
            .collect(Collectors.toSet());
    }
}