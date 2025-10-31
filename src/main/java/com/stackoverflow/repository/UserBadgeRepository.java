package com.stackoverflow.repository;

import com.stackoverflow.entity.Badge;
import com.stackoverflow.entity.User;
import com.stackoverflow.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    List<UserBadge> findByUserOrderByAwardedAtDesc(User user);
    
    boolean existsByUserAndBadge(User user, Badge badge);
    
    long countByUser(User user);
    
    List<UserBadge> findByBadge(Badge badge);
}

