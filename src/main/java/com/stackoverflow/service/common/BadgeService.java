package com.stackoverflow.service.common;

import com.stackoverflow.entity.Badge;
import com.stackoverflow.entity.User;
import com.stackoverflow.entity.UserBadge;
import com.stackoverflow.repository.BadgeRepository;
import com.stackoverflow.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Badge Service - Quản lý huy hiệu và điểm thưởng
 */
@Service
@Transactional
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;
    
    @Autowired
    private UserBadgeRepository userBadgeRepository;

    /**
     * Award badge to user
     */
    public void awardBadge(User user, String badgeName) {
        Badge badge = badgeRepository.findByName(badgeName).orElse(null);
        if (badge == null || userBadgeRepository.existsByUserAndBadge(user, badge)) {
            return; // Badge doesn't exist or user already has it
        }
        
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setReason("Earned through platform activity");
        
        userBadgeRepository.save(userBadge);
        
        // Increment badge earned count
        badge.incrementEarnedCount();
        badgeRepository.save(badge);
        
        // Award points based on badge type
        int points = switch (badge.getType().toUpperCase()) {
            case "GOLD" -> 100;
            case "SILVER" -> 50;
            case "BRONZE" -> 25;
            default -> 10;
        };
        
        user.addPoints(points);
    }

    /**
     * Get all badges for a user
     */
    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByAwardedAtDesc(user);
    }
    
    /**
     * Get all available badges
     */
    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }
    
    /**
     * Create new badge (Admin)
     */
    public Badge createBadge(Badge badge) {
        return badgeRepository.save(badge);
    }
    
    /**
     * Check if user has specific badge
     */
    public boolean hasBadge(User user, String badgeName) {
        Badge badge = badgeRepository.findByName(badgeName).orElse(null);
        if (badge == null) return false;
        return userBadgeRepository.existsByUserAndBadge(user, badge);
    }
}

