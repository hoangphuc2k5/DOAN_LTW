package com.stackoverflow.service.common;

import com.stackoverflow.entity.Notification;
import com.stackoverflow.entity.User;
import com.stackoverflow.entity.UserGroup;
import com.stackoverflow.entity.Question;
import com.stackoverflow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
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

    public void notifyGroup(UserGroup group, String message, String type) {
        messagingTemplate.convertAndSend(
            "/topic/group." + group.getId(),
            new GroupNotification(type, message)
        );
        
        // Also notify all group members individually
        group.getMembers().forEach(member -> {
            notifyUser(member, 
                      "Group: " + group.getName(), 
                      message,
                      type);
        });
    }

    public void notifyNewPost(UserGroup group, Question question) {
        GroupNotification notification = new GroupNotification(
            "NEW_POST",
            createPostNotification(question)
        );
        
        messagingTemplate.convertAndSend(
            "/topic/group." + group.getId(),
            notification
        );
    }

    public void notifyNewComment(User author, Question question, String commentText) {
        notifyUser(question.getAuthor(),
                  "New Comment",
                  author.getUsername() + " commented on your question: " + commentText,
                  "COMMENT");
    }

    private static class GroupNotification {
        private String type;
        private Object data;

        public GroupNotification(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        // Getters
        public String getType() { return type; }
        public Object getData() { return data; }
    }

    private Object createPostNotification(Question question) {
        return Map.of(
            "id", question.getId(),
            "title", question.getTitle(),
            "author", question.getAuthor().getUsername(),
            "timestamp", question.getCreatedAt()
        );
    }
}