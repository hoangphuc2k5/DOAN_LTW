package com.stackoverflow.service.common;

import com.stackoverflow.entity.Attachment;
import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.AttachmentRepository;
import com.stackoverflow.repository.MessageRepository;
import com.stackoverflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Message Service - Tin nhắn riêng
 *
 * Bổ sung:
 * - ConversationSummary DTO + getConversationSummaries(User)
 */
@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * DTO summary for conversation list
     */
    public static class ConversationSummary {
        private User partner;
        private Message lastMessage;
        private LocalDateTime lastTimestamp;
        private long unreadCount;

        public ConversationSummary(User partner, Message lastMessage, LocalDateTime lastTimestamp, long unreadCount) {
            this.partner = partner;
            this.lastMessage = lastMessage;
            this.lastTimestamp = lastTimestamp;
            this.unreadCount = unreadCount;
        }

        public User getPartner() {
            return partner;
        }

        public Message getLastMessage() {
            return lastMessage;
        }

        public LocalDateTime getLastTimestamp() {
            return lastTimestamp;
        }

        public long getUnreadCount() {
            return unreadCount;
        }
    }

    /**
     * Gửi tin nhắn (persist) - giữ nguyên signature hỗ trợ attachments
     */
    public Message sendMessage(User sender, User receiver, String subject, String body, List<MultipartFile> attachments) throws Exception {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
     //   message.setSubject(subject == null ? "" : subject);
        message.setBody(body == null ? "" : body);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        if (attachments != null && !attachments.isEmpty()) {
            List<Attachment> savedAttachments = new ArrayList<>();
            for (MultipartFile f : attachments) {
                if (f == null || f.isEmpty()) continue;
                Attachment a = fileStorageService.store(f);
                a.setMessage(saved);
                savedAttachments.add(a);
            }
            attachmentRepository.saveAll(savedAttachments);
            saved.setAttachments(savedAttachments);
        }

        return saved;
    }

    public Message sendMessage(User sender, User receiver, String subject, String body) throws Exception {
        return sendMessage(sender, receiver, subject, body, Collections.emptyList());
    }

    public Page<Message> getConversation(User a, User b, Pageable pageable) {
        return messageRepository.findConversation(a, b, pageable);
    }

    public Optional<Message> getMessageOptional(Long id) {
        return messageRepository.findByIdWithSenderAndReceiver(id);
    }

    public Message getMessage(Long id) {
        return messageRepository.findByIdWithSenderAndReceiver(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public long countUnreadFromSender(User sender, User receiver) {
        return messageRepository.countBySenderAndReceiverAndIsReadFalse(sender, receiver);
    }

    /**
     * Build conversation summaries (partner, lastMessage, lastTimestamp, unreadCount)
     * Sorted by lastTimestamp DESC (most recent first).
     */
    public List<ConversationSummary> getConversationSummaries(User user) {
        if (user == null || user.getId() == null) return List.of();

        // get partner users (safe JPQL in repository)
        List<User> partners = messageRepository.findConversationPartnersByUserId(user.getId());
        if (partners == null || partners.isEmpty()) return List.of();

        List<ConversationSummary> summaries = new ArrayList<>();
        for (User partner : partners) {
            // get last message between user and partner (use findConversationDesc with page size 1)
            List<Message> lastList = messageRepository.findConversationDesc(user, partner, PageRequest.of(0,1));
            Message lastMessage = (lastList != null && !lastList.isEmpty()) ? lastList.get(0) : null;
            LocalDateTime ts = lastMessage != null ? lastMessage.getCreatedAt() : null;

            long unread = countUnreadFromSender(partner, user); // unread messages FROM partner -> user

            ConversationSummary s = new ConversationSummary(partner, lastMessage, ts, unread);
            summaries.add(s);
        }

        summaries.sort(Comparator.comparing((ConversationSummary s) -> s.getLastTimestamp(), Comparator.nullsLast(Comparator.reverseOrder())));
        return summaries;
    }

    public void markConversationAsRead(User receiver, User sender) {
        Page<Message> page = messageRepository.findConversation(receiver, sender, Pageable.unpaged());
        List<Message> messagesToMark = page.getContent().stream()
                .filter(m -> m.getReceiver() != null && m.getReceiver().getId().equals(receiver.getId()))
                .filter(m -> Boolean.FALSE.equals(m.getIsRead()))
                .collect(Collectors.toList());

        if (!messagesToMark.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            messagesToMark.forEach(m -> {
                m.setIsRead(true);
                m.setReadAt(now);
            });
            messageRepository.saveAll(messagesToMark);
        }
    }

    public Message recallMessage(Long id, User currentUser) {
        Message m = getMessage(id);
        if (!m.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only sender can recall the message");
        }
        m.setBody("[Tin nhắn đã được thu hồi]");
        m.getAttachments().clear();
        return messageRepository.save(m);
    }

    public void deleteMessage(Long id, User currentUser) {
        Message m = getMessage(id);
        boolean changed = false;
        if (m.getSender().getId().equals(currentUser.getId())) {
            if (!Boolean.TRUE.equals(m.getIsDeletedBySender())) {
                m.setIsDeletedBySender(true);
                changed = true;
            }
        }

        if (m.getReceiver().getId().equals(currentUser.getId())) {
            if (!Boolean.TRUE.equals(m.getIsDeletedByReceiver())) {
                m.setIsDeletedByReceiver(true);
                changed = true;
            }
        }

        if (!changed) {
            throw new RuntimeException("You don't have permission to delete this message");
        }

        if (Boolean.TRUE.equals(m.getIsDeletedBySender()) && Boolean.TRUE.equals(m.getIsDeletedByReceiver())) {
            messageRepository.delete(m);
        } else {
            messageRepository.save(m);
        }
    }
}