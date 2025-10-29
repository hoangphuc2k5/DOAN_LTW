/*package com.stackoverflow.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "Forgot Password - Stack Overflow Clone");
        return "auth/forgot-password"; // khớp tên file view trong templates/auth/
    }

    @PostMapping("/password/forgot")
    public String handleForgot(@RequestParam("email") String email,
                               RedirectAttributes ra) {
        // Tạm thời mô phỏng gửi email reset
        // Sau này m có thể thêm chức năng gửi OTP/token thật
        ra.addFlashAttribute("message",
            "Nếu email tồn tại, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.");
        return "redirect:/login";
    }
}
*/
