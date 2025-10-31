package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Attachment;
import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.FileStorageService;
import com.stackoverflow.service.common.MessageService;
import com.stackoverflow.service.common.UserService;
import com.stackoverflow.service.common.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Message Controller - HTTP endpoints + STOMP handlers for chat.
 * Updated to use MessageService.getConversationSummaries(...) (returns DTO) so template can iterate safely.
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private FileStorageService fileStorageService;

    // ChatPayload inner DTO
    public static class ChatPayload {
        private String recipientUsername;
        private String content;
        private String subject;
        private List<String> attachmentFilenames;

        public ChatPayload() {}

        public String getRecipientUsername() { return recipientUsername; }
        public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public List<String> getAttachmentFilenames() { return attachmentFilenames; }
        public void setAttachmentFilenames(List<String> attachmentFilenames) { this.attachmentFilenames = attachmentFilenames; }
    }

    @GetMapping("/inbox")
    public String inbox(Principal principal, Model model) {
        if (principal == null) throw new RuntimeException("Not authenticated");
        User current = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String,Object>> conversations = new ArrayList<>();
        // Use MessageService.getConversationSummaries
        var summaries = messageService.getConversationSummaries(current);
        for (MessageService.ConversationSummary s : summaries) {
            Map<String,Object> item = new HashMap<>();
            item.put("username", s.getPartner().getUsername());
            item.put("displayName", s.getPartner().getUsername());
            item.put("avatar", s.getPartner().getProfileImage() != null ? s.getPartner().getProfileImage() : "/images/default-avatar.png");
            if (s.getLastMessage() != null) {
                item.put("lastMessage", s.getLastMessage().getBody());
                item.put("lastTimestamp", s.getLastTimestamp());
            } else {
                item.put("lastMessage", "");
                item.put("lastTimestamp", null);
            }
            item.put("unread", s.getUnreadCount());
            conversations.add(item);
        }

        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", current);
        model.addAttribute("pageTitle", "Tin nhắn");
        return "messages/inbox";
    }

    // Other endpoints (apiConversation, upload, attachments, STOMP handler, mark-read, recall, delete)
    // For brevity reuse existing methods previously provided — ensure they call messageService methods implemented above.
    // (If you need the full file with all endpoints included again, I can paste the complete controller.)
}