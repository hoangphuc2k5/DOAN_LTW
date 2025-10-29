package com.stackoverflow.controller.admin;

import com.stackoverflow.service.common.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Activity Log Controller - Theo dõi hoạt động
 */
@Controller
@RequestMapping("/admin/activity-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Danh sách activity logs
     */
    @GetMapping
    public String listActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            Model model) {
        
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        var logs = activityLogService.getAllLogs(pageable);
        
        if (search != null && !search.isEmpty()) {
            logs = activityLogService.searchLogs(search, pageable);
            model.addAttribute("search", search);
        } else if (action != null && !action.isEmpty()) {
            logs = activityLogService.getLogsByAction(action, pageable);
            model.addAttribute("action", action);
        }
        
        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("totalItems", logs.getTotalElements());
        
        // Statistics
        model.addAttribute("totalLogs", activityLogService.countAll());
        model.addAttribute("logsToday", activityLogService.countToday());
        model.addAttribute("loginCount", activityLogService.countByAction("LOGIN"));
        model.addAttribute("questionCount", activityLogService.countByAction("CREATE_QUESTION"));
        
        model.addAttribute("pageTitle", "Activity Logs - Admin");
        
        return "admin/activity-logs/list";
    }
}

