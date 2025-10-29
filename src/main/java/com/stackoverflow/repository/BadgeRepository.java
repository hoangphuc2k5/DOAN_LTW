package com.stackoverflow.repository;

import com.stackoverflow.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    Optional<Badge> findByName(String name);
    
    List<Badge> findByType(String type);
    
    List<Badge> findByCategory(String category);
    
    List<Badge> findByIsActive(Boolean isActive);
}
