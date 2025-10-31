package com.stackoverflow.repository;

import com.stackoverflow.entity.Message;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    Page<Message> findBySenderOrderByCreatedAtDesc(User sender, Pageable pageable);

    List<Message> findByReceiverAndIsReadFalse(User receiver);

    long countByReceiverAndIsReadFalse(User receiver);

    // For user deletion
    void deleteBySender(User sender);
    void deleteByReceiver(User receiver);

    // Avoid lazy problems: load sender & receiver when fetching single message
    @EntityGraph(attributePaths = {"sender", "receiver", "attachments"})
    @Query("select m from Message m where m.id = :id")
    Optional<Message> findByIdWithSenderAndReceiver(@Param("id") Long id);

    // Conversation between two users, ordered ascending (oldest first) - paginated
    @EntityGraph(attributePaths = {"sender", "receiver", "attachments"})
    @Query("select m from Message m where (m.sender = :u1 and m.receiver = :u2) or (m.sender = :u2 and m.receiver = :u1) order by m.createdAt asc")
    Page<Message> findConversation(@Param("u1") User u1, @Param("u2") User u2, Pageable pageable);

    // Conversation ordered descending; useful to get latest message (use with PageRequest.of(0,1))
    @EntityGraph(attributePaths = {"sender", "receiver", "attachments"})
    @Query("select m from Message m where (m.sender = :u1 and m.receiver = :u2) or (m.sender = :u2 and m.receiver = :u1) order by m.createdAt desc")
    List<Message> findConversationDesc(@Param("u1") User u1, @Param("u2") User u2, Pageable pageable);

    // Count unread messages where sender -> receiver and isRead = false
    long countBySenderAndReceiverAndIsReadFalse(User sender, User receiver);

    /**
     * SAFE: Find conversation partner Users for a given user id.
     * Implementation uses a subquery that returns partner IDs (CASE on ids),
     * then selects Users where id IN (subquery). This avoids returning entities
     * directly from CASE expression (which caused ClassCastException on Hibernate 6).
     */
    @Query("select u from User u where u.id in (" +
            " select case when m.sender.id = :meId then m.receiver.id else m.sender.id end " +
            " from Message m where m.sender.id = :meId or m.receiver.id = :meId" +
            ")")
    List<com.stackoverflow.entity.User> findConversationPartnersByUserId(@Param("meId") Long meId);
}