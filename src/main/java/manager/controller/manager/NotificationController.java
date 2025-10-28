package manager.controller.manager;

import manager.entity.Notification;
import manager.service.NotificationService;
import manager.service.ActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/notifications")
public class NotificationController {

    private final NotificationService notiService;
    private final ActivityLogService logService;

    public NotificationController(NotificationService notiService, ActivityLogService logService) {
        this.notiService = notiService;
        this.logService = logService;
    }

    // 🔹 Danh sách thông báo
    @GetMapping
    public String listNotifications(@RequestParam(defaultValue = "1") Long userId, Model model) {
        List<Notification> notis = notiService.getUserNotifications(userId);
        model.addAttribute("notifications", notis);
        return "manager/notification/list";
    }

    // 🔹 Form gửi thông báo
    @GetMapping("/send")
    public String showSendForm(Model model) {
        model.addAttribute("notification", new Notification());
        return "manager/notification/send-form";
    }

    // 🔹 Gửi thông báo
    @PostMapping("/send")
    public String sendNotification(@RequestParam String message,
                                   @RequestParam(required = false) String link,
                                   @RequestParam(defaultValue = "SYSTEM") String type,
                                   @RequestParam(defaultValue = "1") Long senderId,
                                   @RequestParam(defaultValue = "all") String target) {
        if ("all".equalsIgnoreCase(target)) {
            notiService.notifyAllUsers(type, message, link, senderId);
            logService.logAction(senderId, "SEND_GLOBAL_NOTIFICATION", "NOTIFICATION", null,
                    "Manager sent global notification");
        } else {
            try {
                Long userId = Long.parseLong(target);
                notiService.notifyUser(userId, type, message, link, senderId);
                logService.logAction(senderId, "SEND_USER_NOTIFICATION", "NOTIFICATION", userId,
                        "Sent notification to user " + userId);
            } catch (NumberFormatException ignored) {}
        }
        return "redirect:/manager/notifications/send?success";
    }
}
