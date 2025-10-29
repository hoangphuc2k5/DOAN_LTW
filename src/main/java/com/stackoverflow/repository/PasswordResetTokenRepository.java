package com.stackoverflow.repository;

import com.stackoverflow.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("""
      SELECT t FROM PasswordResetToken t
      WHERE t.email = :email AND t.otpCode = :otp
        AND t.used = false AND t.expiresAt >= :now
      ORDER BY t.createdAt DESC
    """)
    Optional<PasswordResetToken> findValid(String email, String otp, LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.email = :email AND t.otpCode = :otp")
    int markUsed(String email, String otp);
}
