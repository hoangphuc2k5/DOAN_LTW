package manager.service.impl;

import manager.entity.Notification;
import manager.entity.User;
import manager.repository.NotificationRepository;
import manager.repository.UserRepository;
import manager.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public NotificationServiceImpl(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void notifyUser(Long userId, String type, String message, String link, Long senderId) {
        User receiver = userRepo.findById(userId).orElse(null);
        if (receiver == null) return;
        User u= new User();
        u.setId(userId);
        Notification n = new Notification();
        n.setUser(receiver);
        n.setType(type);
        n.setMessage(message);
        n.setLink(link);
        n.setSender(u);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        n.setGlobal(false);
        notificationRepo.save(n);
    }

    @Override
    public void notifyAllUsers(String type, String message, String link, Long senderId) {
        List<User> allUsers = userRepo.findAll();
        for (User u : allUsers) {
            Notification n = new Notification();
            User sender = new User();
            sender.setId(senderId);
            n.setUser(u);
            n.setType(type);
            n.setMessage(message);
            n.setLink(link);
            n.setSender(sender);
            n.setCreatedAt(LocalDateTime.now());
            n.setRead(false);
            n.setGlobal(true);
            notificationRepo.save(n);
        }
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepo.findById(notificationId).orElse(null);
        if (n != null && !n.getIsRead()) {
            n.markAsRead();
            notificationRepo.save(n);
        }
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepo.countByUserIdAndIsReadFalse(userId);
    }
}
