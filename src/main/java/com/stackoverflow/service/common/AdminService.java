package com.stackoverflow.service.common;

import com.stackoverflow.entity.User;
import com.stackoverflow.entity.Question;
import com.stackoverflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Admin Service - Quản lý người dùng
 */
@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    // ================== QUẢN LÝ NGƯỜI DÙNG ==================

    /**
     * Lấy tất cả người dùng (có phân trang)
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Lấy tất cả người dùng (không phân trang)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Tìm người dùng theo ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Tìm người dùng theo username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Tìm kiếm người dùng theo từ khóa (username hoặc email)
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pageable);
    }

    /**
     * Lấy người dùng theo vai trò
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * Khóa tài khoản người dùng
     */
    public void banUser(Long userId, String reason, LocalDateTime bannedUntil) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsBanned(true);
        user.setBanReason(reason);
        user.setBannedUntil(bannedUntil);
        
        userRepository.save(user);
    }

    /**
     * Khóa tài khoản vĩnh viễn
     */
    public void banUserPermanently(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsBanned(true);
        user.setBanReason(reason);
        user.setBannedUntil(null); // null = permanent ban
        
        userRepository.save(user);
    }

    /**
     * Mở khóa tài khoản
     */
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedUntil(null);
        
        userRepository.save(user);
    }

    /**
     * Vô hiệu hóa tài khoản (không cho đăng nhập nhưng không xóa dữ liệu)
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Kích hoạt lại tài khoản
     */
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Đổi mật khẩu cho người dùng (Admin reset password)
     */
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Thay đổi vai trò của người dùng
     */
    public void changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * Xóa người dùng (cẩn thận!)
     * Xóa tất cả dữ liệu liên quan trước
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete related entities first to avoid FK constraint violations
        // Note: This will delete ALL user data permanently!
        
        // 1. Delete notifications
        notificationRepository.deleteByUser(user);
        
        // 2. Delete activity logs
        activityLogRepository.deleteByUser(user);
        
        // 3. Delete messages (sent and received)
        messageRepository.deleteBySender(user);
        messageRepository.deleteByReceiver(user);
        
        // 4. Delete reports
        reportRepository.deleteByReporter(user);
        
        // 5. Delete questions manually (to properly handle question_tags FK constraint)
        // Query all questions by this user and delete them one by one
        List<Question> userQuestions = questionRepository.findByAuthor(user);
        System.out.println("Deleting " + userQuestions.size() + " questions for user " + user.getUsername());
        
        for (Question question : userQuestions) {
            try {
                questionService.deleteQuestion(question.getId());
                System.out.println("Deleted question ID: " + question.getId());
            } catch (Exception e) {
                System.err.println("Error deleting question " + question.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 6. Answers and Comments will be cascade deleted via JPA @OneToMany mappings
        
        // Finally, delete the user
        System.out.println("Deleting user ID: " + userId);
        userRepository.deleteById(userId);
        System.out.println("Successfully deleted user: " + user.getUsername());
    }

    /**
     * Lấy số lượng người dùng theo trạng thái
     */
    public long countActiveUsers() {
        return userRepository.countByIsActive(true);
    }

    public long countBannedUsers() {
        return userRepository.countByIsBanned(true);
    }

    public long countUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    /**
     * Xác minh email cho người dùng
     */
    public void verifyUserEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}

