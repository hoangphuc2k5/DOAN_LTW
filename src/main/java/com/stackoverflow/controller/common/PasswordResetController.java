package com.stackoverflow.controller.common;

import com.stackoverflow.service.common.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping("/forgot")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot")
    public String sendOTP(@RequestParam String email, Model model) {
        try {
            resetService.sendPasswordResetOTP(email);
            model.addAttribute("email", email);
            model.addAttribute("message", "OTP đã được gửi đến email của bạn!");
            return "auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @PostMapping("/verify")
    public String verifyOTP(@RequestParam String email,
                            @RequestParam String otp,
                            @RequestParam String newPassword,
                            Model model) {
        try {
            resetService.resetPassword(email, otp, newPassword);
            return "redirect:/login?reset_success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "auth/verify-otp";
        }
    }
}
