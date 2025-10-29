package com.stackoverflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", 
    indexes = {
        @Index(name = "idx_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_action_created", columnList = "action,created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action; // CREATE_QUESTION, POST_ANSWER, VOTE, LOGIN, ADMIN_ACTION, etc.

    @Column(nullable = false)
    private String entityType; // QUESTION, ANSWER, COMMENT, USER, SYSTEM

    private Long entityId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String details;

    private String ipAddress;

    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

