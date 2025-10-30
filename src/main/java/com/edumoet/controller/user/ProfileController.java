package com.edumoet.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edumoet.entity.Question;
import com.edumoet.entity.User;
import com.edumoet.service.common.AnswerService;
import com.edumoet.service.common.QuestionService;
import com.edumoet.service.common.UserService;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Profile Controller - Quản lý hồ sơ cá nhân (dùng AWS S3 cho avatar)
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    // 🟢 AWS S3 dependencies
    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-folder:ltWeb}")
    private String baseFolder;

    /**
     * Xem profile của mình
     */
    @GetMapping
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Question> questions = questionService.getQuestionsByAuthor(user, pageable);
        Page<com.edumoet.entity.Answer> answers = answerService.getAnswersByAuthor(user, pageable);

        Long totalQuestions = questionService.countByAuthor(user);
        Long totalAnswers = answerService.countByAuthor(user);

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalAnswers", totalAnswers);
        model.addAttribute("totalComments", 0L);
        model.addAttribute("activities", List.of());
        model.addAttribute("badges", List.of());
        model.addAttribute("pageTitle", "My Profile");

        return "profile/view";
    }

    /**
     * Form chỉnh sửa profile
     */
    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");
        return "profile/edit";
    }

    /**
     * Cập nhật profile (Upload avatar lên S3)
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String githubUrl,
            @RequestParam(required = false) String linkedinUrl,
            @RequestParam(required = false) MultipartFile profilePicture,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setEmail(email);
            user.setBio(bio);
            user.setLocation(location);
            user.setWebsite(website);
            user.setGithubUrl(githubUrl);
            user.setLinkedinUrl(linkedinUrl);

            // ================== Upload Avatar lên S3 ==================
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String contentType = profilePicture.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("File must be an image");
                }
                if (profilePicture.getSize() > 5 * 1024 * 1024) {
                    throw new IllegalArgumentException("File size must be less than 5MB");
                }

                // Tạo tên file duy nhất
                String originalFilename = profilePicture.getOriginalFilename();
                String extension = (originalFilename != null && originalFilename.contains("."))
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String uuid = UUID.randomUUID().toString().substring(0, 8);
                String filename = String.format("user%d_%s_%s%s", user.getId(), timestamp, uuid, extension);
                String key = baseFolder + "/avatars/" + filename;

                // Xoá ảnh cũ trên S3 nếu có
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(baseFolder + "/avatars/" + user.getProfileImage())
                                .build());
                        System.out.println("🗑️ Deleted old avatar from S3");
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not delete old avatar: " + e.getMessage());
                    }
                }

                // Upload ảnh mới
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        RequestBody.fromBytes(profilePicture.getBytes())
                );

                user.setProfileImage(filename);
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Profile updated successfully!");
            return "redirect:/profile";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/profile/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Error: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Xóa ảnh đại diện (trên S3)
     */
    @PostMapping("/remove-avatar")
    public String removeAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                String key = baseFolder + "/avatars/" + user.getProfileImage();

                try {
                    s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build());
                    System.out.println("🗑️ Deleted avatar from S3: " + key);
                } catch (Exception e) {
                    System.out.println("⚠️ Failed to delete avatar: " + e.getMessage());
                }

                user.setProfileImage(null);
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Profile picture removed successfully!");
            }

            return "redirect:/profile/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Error: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Form đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("pageTitle", "Change Password");
        return "profile/change-password";
    }

    /**
     * Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Current password is incorrect!");
                return "redirect:/profile/change-password";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "❌ New passwords do not match!");
                return "redirect:/profile/change-password";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Password changed successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Error: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    /**
     * Xem profile người khác
     */
    @GetMapping("/{username}")
    public String viewUserProfile(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", username + "'s Profile");
        return "profile/public-view";
    }

    /**
     * Danh sách câu hỏi của tôi
     */
    @GetMapping("/my-questions")
    public String myQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "all") String filter,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Question> questions;

        switch (filter) {
            case "pending":
                questions = questionService.getPendingQuestionsByAuthor(currentUser, pageable);
                break;
            case "approved":
                questions = questionService.getApprovedQuestionsByAuthor(currentUser, pageable);
                break;
            default:
                questions = questionService.getQuestionsByAuthor(currentUser, pageable);
                break;
        }

        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("filter", filter);
        model.addAttribute("pageTitle", "My Questions - EDUMOET");

        return "profile/my-questions";
    }
}
