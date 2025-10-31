package com.stackoverflow.service.common;

import com.stackoverflow.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String username, Object payload) {
        messagingTemplate.convertAndSendToUser(username, "/queue/messages", payload);
    }

    public void broadcastToTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + topic, payload);
    }
}