package com.stackoverflow.repository;

import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);
    
    Page<Message> findBySenderOrderByCreatedAtDesc(User sender, Pageable pageable);
    
    List<Message> findByReceiverAndIsReadFalse(User receiver);
    
    long countByReceiverAndIsReadFalse(User receiver);
    
    // For user deletion
    void deleteBySender(User sender);
    void deleteByReceiver(User receiver);
}
