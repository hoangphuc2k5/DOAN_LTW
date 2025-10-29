package com.stackoverflow.service.common;

import com.stackoverflow.entity.ActivityLog;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Activity Log Service - Theo dõi hoạt động người dùng
 */
@Service
@Transactional
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    /**
     * Ghi log hoạt động
     */
    public void log(User user, String action, String entityType, Long entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress("0.0.0.0"); // TODO: Get real IP
        log.setCreatedAt(LocalDateTime.now());
        
        activityLogRepository.save(log);
    }

    /**
     * Log đơn giản
     */
    public void logAction(User user, String action, String details) {
        log(user, action, null, null, details);
    }

    /**
     * Lấy tất cả log
     */
    public Page<ActivityLog> getAllLogs(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }

    /**
     * Lấy log của 1 user
     */
    public Page<ActivityLog> getLogsByUser(User user, Pageable pageable) {
        return activityLogRepository.findByUser(user, pageable);
    }

    /**
     * Lấy log theo action
     */
    public Page<ActivityLog> getLogsByAction(String action, Pageable pageable) {
        return activityLogRepository.findByAction(action, pageable);
    }

    /**
     * Tìm kiếm log
     */
    public Page<ActivityLog> searchLogs(String keyword, Pageable pageable) {
        return activityLogRepository.findByDetailsContaining(keyword, pageable);
    }

    /**
     * Xóa log cũ (sau 90 ngày)
     */
    public void cleanOldLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        activityLogRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    /**
     * Thống kê
     */
    public long countAll() {
        return activityLogRepository.count();
    }

    public long countByAction(String action) {
        return activityLogRepository.countByAction(action);
    }

    public long countToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return activityLogRepository.countByCreatedAtAfter(startOfDay);
    }
}

