package manager.service;

import manager.entity.Notification;
import java.util.List;

public interface NotificationService {

    // 🔹 Gửi thông báo đến 1 người dùng
    void notifyUser(Long userId, String type, String message, String link, Long senderId);

    // 🔹 Gửi thông báo đến tất cả người dùng
    void notifyAllUsers(String type, String message, String link, Long senderId);

    // 🔹 Lấy danh sách thông báo của 1 người dùng
    List<Notification> getUserNotifications(Long userId);

    // 🔹 Đánh dấu đã đọc
    void markAsRead(Long notificationId);

    // 🔹 Đếm số thông báo chưa đọc
    long countUnread(Long userId);
}
