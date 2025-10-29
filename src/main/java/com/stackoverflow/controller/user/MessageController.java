package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.MessageService;
import com.stackoverflow.service.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Message Controller - Tin nhắn riêng
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * Inbox - Hộp thư đến
     */
    @GetMapping("/inbox")
    public String inbox(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, 20);
        var messages = messageService.getReceivedMessages(user, pageable);
        
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("unreadCount", messageService.countUnread(user));
        model.addAttribute("pageTitle", "Inbox");
        
        return "messages/inbox";
    }

    /**
     * Sent - Hộp thư đã gửi
     */
    @GetMapping("/sent")
    public String sent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        User user = userService.findByUsername(userDetails.getUsername())
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
     * Compose - Soạn tin nhắn mới
     */
    @GetMapping("/compose")
    public String compose(
            @RequestParam(required = false) String to,
            Model model) {
        
        model.addAttribute("recipientUsername", to);
        model.addAttribute("pageTitle", "Compose Message");
        
        return "messages/compose";
    }

    /**
     * Send message
     */
    @PostMapping("/send")
    public String sendMessage(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            User sender = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            
            User receiver = userService.findByUsername(to)
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
            
            messageService.sendMessage(sender, receiver, subject, body);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Message sent successfully!");
            
            return "redirect:/messages/sent";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error: " + e.getMessage());
            return "redirect:/messages/compose";
        }
    }

    /**
     * Xem tin nhắn
     */
    @GetMapping("/{id}")
    public String viewMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Message message = messageService.getMessage(id);
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check permission
            if (!message.getReceiver().equals(currentUser) && !message.getSender().equals(currentUser)) {
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
     * Xóa tin nhắn
     */
    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            messageService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Message deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/messages/inbox";
    }
}

