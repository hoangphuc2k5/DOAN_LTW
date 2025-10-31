package com.stackoverflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // sender
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // receiver (1-1 chat)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isDeletedBySender = false;

    @Column(nullable = false)
    private Boolean isDeletedByReceiver = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    // helper
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Boolean getIsDeletedBySender() {
		return isDeletedBySender;
	}

	public void setIsDeletedBySender(Boolean isDeletedBySender) {
		this.isDeletedBySender = isDeletedBySender;
	}

	public Boolean getIsDeletedByReceiver() {
		return isDeletedByReceiver;
	}

	public void setIsDeletedByReceiver(Boolean isDeletedByReceiver) {
		this.isDeletedByReceiver = isDeletedByReceiver;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getReadAt() {
		return readAt;
	}

	public void setReadAt(LocalDateTime readAt) {
		this.readAt = readAt;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
    
}