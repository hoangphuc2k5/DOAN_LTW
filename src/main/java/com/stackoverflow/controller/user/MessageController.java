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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ChatMessage DTO for WebSocket real-time chat
    public static class ChatMessage {
        private String from;
        private String to;
        private String content;
        private LocalDateTime timestamp;
        private Long messageId;

        public ChatMessage() {}
        
        public ChatMessage(String from, String to, String content) {
            this.from = from;
            this.to = to;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }
    }
    
    // ChatPayload inner DTO for traditional messaging
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

    /**
     * Inbox - Hộp thư đến (Conversation view như DOAN_LTW-hao)
     */
    @GetMapping("/inbox")
    public String inbox(Principal principal, Model model) {
        if (principal == null) throw new RuntimeException("Not authenticated");
        User current = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String,Object>> conversations = new ArrayList<>();
        // Use MessageService.getConversationSummaries for conversation view
        var summaries = messageService.getConversationSummaries(current);
        for (MessageService.ConversationSummary s : summaries) {
            Map<String,Object> item = new HashMap<>();
            item.put("username", s.getPartner().getUsername());
            item.put("displayName", s.getPartner().getUsername());
            item.put("avatar", s.getPartner().getProfileImage() != null ? 
                    "/uploads/avatars/" + s.getPartner().getProfileImage() : 
                    "https://ui-avatars.com/api/?name=" + s.getPartner().getUsername() + "&size=48");
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

    /**
     * Sent Messages - Hộp thư đã gửi
     */
    @GetMapping("/sent")
    public String sent(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        if (principal == null) throw new RuntimeException("Not authenticated");
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, 20);
        var messages = messageService.getSentMessages(user, pageable);
        
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("pageTitle", "Sent Messages");
        
        return "messages/sent";
    }

    /**
     * Compose - Redirect to inbox (not needed anymore, modal is used instead)
     */
    @GetMapping("/compose")
    public String compose(
            @RequestParam(required = false) String to,
            Model model) {
        
        // Redirect to inbox where users can use the modal to start new conversations
        return "redirect:/messages/inbox";
    }

    /**
     * Send message
     */
    @PostMapping("/send")
    public String sendMessage(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam(required = false) List<MultipartFile> attachments,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (principal == null) throw new RuntimeException("Not authenticated");
            
            User sender = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            
            User receiver = userService.findByUsername(to)
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
            
            Message message = messageService.sendMessage(sender, receiver, subject, body);
            
            // Handle file attachments if any (Note: Message entity may not have attachments field)
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile file : attachments) {
                    if (!file.isEmpty()) {
                        // Store attachment (can be extended later when Message entity supports attachments)
                        Attachment attachment = fileStorageService.store(file);
                        // attachment.setMessage(message);  // Enable when Message has attachments field
                    }
                }
            }
            
            // Send WebSocket notification
            webSocketService.notifyUser(receiver, 
                "New Message", 
                "You have a new message from " + sender.getUsername(), 
                "MESSAGE");
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Message sent successfully!");
            
            return "redirect:/messages/sent";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
            return "redirect:/messages/compose?to=" + to;
        }
    }

    /**
     * View message
     */
    @GetMapping("/{id}")
    public String viewMessage(
            @PathVariable Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (principal == null) throw new RuntimeException("Not authenticated");
            
            Message message = messageService.getMessage(id);
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check permission
            if (!message.getReceiver().equals(currentUser) && 
                !message.getSender().equals(currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "You don't have permission to view this message");
                return "redirect:/messages/inbox";
            }
            
            // Mark as read if receiver
            if (message.getReceiver().equals(currentUser) && !message.getIsRead()) {
                messageService.markAsRead(id);
            }
            
            model.addAttribute("message", message);
            model.addAttribute("pageTitle", message.getSubject());
            
            return "messages/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/messages/inbox";
        }
    }

    /**
     * Delete message
     */
    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (principal == null) throw new RuntimeException("Not authenticated");
            
            Message message = messageService.getMessage(id);
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check permission
            if (!message.getReceiver().equals(currentUser) && 
                !message.getSender().equals(currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "You don't have permission to delete this message");
                return "redirect:/messages/inbox";
            }
            
            messageService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Message deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/messages/inbox";
    }

    /**
     * Search users (API for compose form and new message modal)
     */
    @GetMapping("/api/search-users")
    @ResponseBody
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query,
            Principal principal) {
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<User> users;
            
            if (query == null || query.trim().isEmpty()) {
                // Return all users (or top users by reputation)
                Pageable pageable = PageRequest.of(0, 50);
                users = userService.getAllUsers(pageable).getContent();
            } else if (query.trim().length() < 2) {
                // Query too short, return empty
                return ResponseEntity.ok(Collections.emptyList());
            } else {
                // Search users by username or email
                users = userService.searchByUsernameOrEmail(query.trim());
            }
            
            // Get current user to exclude from results
            String currentUsername = principal.getName();
            
            // Convert to simple DTO for response
            List<Map<String, Object>> results = users.stream()
                .filter(user -> !user.getUsername().equals(currentUsername)) // Exclude current user
                .limit(50) // Limit to 50 results
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("username", user.getUsername());
                    dto.put("email", user.getEmail());
                    dto.put("reputation", user.getReputation());
                    dto.put("avatar", user.getProfileImage() != null ? 
                        "/uploads/avatars/" + user.getProfileImage() : 
                        "https://ui-avatars.com/api/?name=" + user.getUsername() + "&size=32");
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Download attachment
     */
    @GetMapping("/attachments/{filename:.+}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadAsResource(filename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark message as read (AJAX)
     */
    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            messageService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get conversation with a user (API for AJAX)
     */
    @GetMapping("/api/conversation/{username}")
    @ResponseBody
    public ResponseEntity<?> getConversation(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User partner = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            
            // Get messages between two users
            List<Message> messages = messageService.getConversationMessages(currentUser, partner);
            
            // Convert to DTO
            List<Map<String, Object>> result = messages.stream()
                .map(msg -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", msg.getId());
                    dto.put("from", msg.getSender().getUsername());
                    dto.put("to", msg.getReceiver().getUsername());
                    dto.put("content", msg.getBody());
                    dto.put("timestamp", msg.getCreatedAt());
                    dto.put("isRead", msg.getIsRead());
                    dto.put("isMine", msg.getSender().equals(currentUser));
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * WebSocket endpoint - Send real-time chat message
     */
    @MessageMapping("/chat.send")
    public void sendChatMessage(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            if (principal == null) return;
            
            // Get sender and receiver
            User sender = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            User receiver = userService.findByUsername(chatMessage.getTo())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
            
            // Save message to database
            Message message = messageService.sendMessage(
                sender, 
                receiver, 
                "Chat", 
                chatMessage.getContent()
            );
            
            // Update chat message with ID and actual sender
            chatMessage.setMessageId(message.getId());
            chatMessage.setFrom(sender.getUsername());
            chatMessage.setTimestamp(message.getCreatedAt());
            
            // Send to receiver via WebSocket
            messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/messages",
                chatMessage
            );
            
            // Send back to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                sender.getUsername(),
                "/queue/messages",
                chatMessage
            );
            
        } catch (Exception e) {
            System.err.println("Error sending chat message: " + e.getMessage());
        }
    }
}