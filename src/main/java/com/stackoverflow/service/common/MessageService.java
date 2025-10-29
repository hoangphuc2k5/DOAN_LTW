package com.stackoverflow.service.common;

import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message Service - Tin nhắn riêng
 */
@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Gửi tin nhắn
     */
    public Message sendMessage(User sender, User receiver, String subject, String body) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(subject);
        message.setBody(body);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }

    /**
     * Lấy tin nhắn đã nhận
     */
    public Page<Message> getReceivedMessages(User user, Pageable pageable) {
        return messageRepository.findByReceiverOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Lấy tin nhắn đã gửi
     */
    public Page<Message> getSentMessages(User user, Pageable pageable) {
        return messageRepository.findBySenderOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Lấy tin nhắn chưa đọc
     */
    public List<Message> getUnreadMessages(User user) {
        return messageRepository.findByReceiverAndIsReadFalse(user);
    }

    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    /**
     * Xóa tin nhắn
     */
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    /**
     * Lấy 1 tin nhắn
     */
    public Message getMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    public long countUnread(User user) {
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }
}

