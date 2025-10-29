package com.stackoverflow.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from); // PHẢI trùng spring.mail.username
        msg.setTo(to);
        msg.setSubject("[StackOverflow Clone] Mã OTP đặt lại mật khẩu");
        msg.setText("Mã OTP của bạn: " + otp + " (hiệu lực 5 phút).");
        mailSender.send(msg);
    }
}
