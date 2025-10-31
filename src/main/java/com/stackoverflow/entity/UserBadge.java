package com.stackoverflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @CreatedDate
    @Column(name = "awarded_at", nullable = false, updatable = false)
    private LocalDateTime awardedAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Badge getBadge() {
		return badge;
	}

	public void setBadge(Badge badge) {
		this.badge = badge;
	}

	public LocalDateTime getAwardedAt() {
		return awardedAt;
	}

	public void setAwardedAt(LocalDateTime awardedAt) {
		this.awardedAt = awardedAt;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
    
}

