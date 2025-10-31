package com.stackoverflow.repository;

import com.stackoverflow.entity.Event;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Page<Event> findByIsActiveTrueOrderByStartDateDesc(Pageable pageable);
    
    List<Event> findByCreatorOrderByCreatedAtDesc(User creator);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startDate <= :now AND e.endDate >= :now ORDER BY e.startDate DESC")
    List<Event> findOngoingEvents(LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.endDate < :now ORDER BY e.endDate DESC")
    Page<Event> findPastEvents(LocalDateTime now, Pageable pageable);
}

