package com.stackoverflow.controller.admin;

import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Admin User Controller - Quản lý người dùng
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    /**
     * Danh sách người dùng
     */
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : 
                    Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users;
        
        if (search != null && !search.isEmpty()) {
            users = adminService.searchUsers(search, pageable);
            model.addAttribute("search", search);
        } else {
            users = adminService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("pageTitle", "Quản Lý Người Dùng - Quản Trị");
        
        return "admin/users/list";
    }

    /**
     * Xem chi tiết người dùng
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = adminService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Chi Tiết Người Dùng - " + user.getUsername());
        
        return "admin/users/view";
    }

    /**
     * Form chỉnh sửa người dùng
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = adminService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Chỉnh Sửa Người Dùng - " + user.getUsername());
        
        return "admin/users/edit";
    }

    /**
     * Xử lý cập nhật thông tin người dùng
     */
    @PostMapping("/{id}/edit")
    public String editUser(
            @PathVariable Long id,
            @RequestParam String location,
            @RequestParam String about,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = adminService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            user.setLocation(location);
            user.setAbout(about);
            adminService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Cập nhật thông tin người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Khóa tài khoản tạm thời
     */
    @PostMapping("/{id}/ban")
    public String banUser(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Violated community guidelines") String reason,
            @RequestParam(required = false) Integer days,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Default reason if empty
            String banReason = (reason == null || reason.trim().isEmpty()) 
                    ? "Violated community guidelines" 
                    : reason;
            
            if (days != null && days > 0) {
                LocalDateTime bannedUntil = LocalDateTime.now().plusDays(days);
                adminService.banUser(id, banReason, bannedUntil);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã khóa tài khoản trong " + days + " ngày!");
            } else {
                adminService.banUserPermanently(id, banReason);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã khóa tài khoản vĩnh viễn!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Mở khóa tài khoản
     */
    @PostMapping("/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.unbanUser(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã mở khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Vô hiệu hóa tài khoản
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã vô hiệu hóa tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Kích hoạt tài khoản
     */
    @PostMapping("/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.activateUser(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã kích hoạt tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Đổi mật khẩu người dùng
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            adminService.resetUserPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đặt lại mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Thay đổi vai trò người dùng
     */
    @PostMapping("/{id}/change-role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        
        try {
            adminService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã thay đổi vai trò thành " + role + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }

    /**
     * Xóa người dùng
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã xóa người dùng!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }
}
