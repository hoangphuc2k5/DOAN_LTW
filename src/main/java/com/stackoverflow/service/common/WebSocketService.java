package com.stackoverflow.service.common;

import com.stackoverflow.entity.Notification;
import com.stackoverflow.entity.User;
import com.stackoverflow.entity.Question;
import com.stackoverflow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationRepository notificationRepository;

    public void notifyUser(User user, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        messagingTemplate.convertAndSendToUser(
            user.getUsername(),
            "/queue/notifications",
            notification
        );
    }

    public void notifyNewComment(User author, Question question, String commentText) {
        notifyUser(question.getAuthor(),
                  "New Comment",
                  author.getUsername() + " commented on your question: " + commentText,
                  "COMMENT");
    }
}