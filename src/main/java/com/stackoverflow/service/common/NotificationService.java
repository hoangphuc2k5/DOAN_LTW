package com.stackoverflow.service.common;

import com.stackoverflow.entity.Notification;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.NotificationRepository;
import com.stackoverflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Gửi thông báo đến 1 người dùng
     */
    public Notification sendToUser(User user, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    /**
     * Gửi thông báo đến TẤT CẢ người dùng
     */
    public int broadcastToAll(String message, String type) {
        List<User> allUsers = userRepository.findAll();
        int count = 0;
        for (User user : allUsers) {
            sendToUser(user, message, type);
            count++;
        }
        return count;
    }

    /**
     * Gửi thông báo đến người dùng theo role
     */
    public int broadcastToRole(String role, String message, String type) {
        List<User> users = userRepository.findByRole(role);
        int count = 0;
        for (User user : users) {
            sendToUser(user, message, type);
            count++;
        }
        return count;
    }

    /**
     * Lấy thông báo của 1 user
     */
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy thông báo chưa đọc
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    public void markAllAsRead(User user) {
        List<Notification> unread = getUnreadNotifications(user);
        for (Notification n : unread) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }

    /**
     * Xóa thông báo
     */
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    /**
     * Admin: Lấy tất cả thông báo
     */
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    /**
     * Admin: Thống kê
     */
    public long countAll() {
        return notificationRepository.count();
    }

    public long countUnread() {
        return notificationRepository.countByIsReadFalse();
    }
}

