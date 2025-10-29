package com.stackoverflow.service.common;

import com.stackoverflow.entity.PasswordResetToken;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.PasswordResetTokenRepository;
import com.stackoverflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RAND = new SecureRandom();

    public PasswordResetService(UserRepository userRepo,
                                PasswordResetTokenRepository tokenRepo,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendPasswordResetOTP(String email) {
        Optional<User> u = userRepo.findByEmail(email);
        if (u.isEmpty()) throw new IllegalArgumentException("Email không tồn tại");

        String otp = String.format("%06d", RAND.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken t = new PasswordResetToken();
        t.setEmail(email);
        t.setOtpCode(otp);
        t.setCreatedAt(now);
        t.setExpiresAt(now.plusMinutes(5));
        t.setUsed(false);
        tokenRepo.save(t);

        emailService.sendOtp(email, otp);
    }

    public boolean verifyOTP(String email, String otp) {
        return tokenRepo.findValid(email, otp, LocalDateTime.now()).isPresent();
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (!verifyOTP(email, otp))
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn");

        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        u.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(u);
        tokenRepo.markUsed(email, otp);
    }
}
