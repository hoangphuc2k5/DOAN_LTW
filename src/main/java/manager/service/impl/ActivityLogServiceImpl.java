package manager.service.impl;

import manager.entity.ActivityLog;
import manager.entity.User;
import manager.repository.ActivityLogRepository;
import manager.repository.UserRepository;
import manager.service.ActivityLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;
    private final UserRepository userRepo;

    public ActivityLogServiceImpl(ActivityLogRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Override
    public void logAction(Long userId, String action, String entityType, Long entityId, String details) {
        User user = userRepo.findById(userId).orElseThrow();
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        repo.save(log);
    }

    @Override
    public List<ActivityLog> findRecent() {
        return repo.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();
    }

    @Override
    public List<ActivityLog> findByUser(Long userId) {
        return repo.findAll()
                .stream()
                .filter(l -> l.getUser() != null && l.getUser().getId().equals(userId))
                .toList();
    }
}
