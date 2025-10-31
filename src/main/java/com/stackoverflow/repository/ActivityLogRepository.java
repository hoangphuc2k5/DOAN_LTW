package com.stackoverflow.repository;

import com.stackoverflow.entity.ActivityLog;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    Page<ActivityLog> findByUser(User user, Pageable pageable);
    
    Page<ActivityLog> findByAction(String action, Pageable pageable);
    
    Page<ActivityLog> findByDetailsContaining(String keyword, Pageable pageable);
    
    void deleteByCreatedAtBefore(LocalDateTime date);
    
    long countByAction(String action);
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    // For user deletion
    void deleteByUser(User user);
}
