package com.stackoverflow.repository;

import com.stackoverflow.entity.ChatbotConversation;
import com.stackoverflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    
    Optional<ChatbotConversation> findBySessionId(String sessionId);
    
    List<ChatbotConversation> findByUserAndIsActiveTrue(User user);
    
    List<ChatbotConversation> findByUser(User user);
    
    @Query("SELECT c FROM ChatbotConversation c WHERE c.isActive = true AND c.startedAt < :before")
    List<ChatbotConversation> findActiveConversationsStartedBefore(@Param("before") LocalDateTime before);
    
    long countByUser(User user);
    
    @Query("SELECT COUNT(c) FROM ChatbotConversation c WHERE c.startedAt >= :from AND c.startedAt <= :to")
    long countByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

