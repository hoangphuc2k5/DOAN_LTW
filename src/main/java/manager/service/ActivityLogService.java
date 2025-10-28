package manager.service;

import manager.entity.ActivityLog;
import java.util.List;

public interface ActivityLogService {
    void logAction(Long userId, String action, String entityType, Long entityId, String details);
    List<ActivityLog> findRecent();
    List<ActivityLog> findByUser(Long userId);
}
