package com.edumoet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

<<<<<<< HEAD
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
=======
    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
>>>>>>> 1370639 ( Done 1.2)
    private String entityType; // QUESTION, ANSWER, COMMENT, USER

    @Column(nullable = false)
    private Long entityId;

    @NotBlank
<<<<<<< HEAD
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
=======
    @Column(nullable = false, columnDefinition = "NVARCHAR(250)")
>>>>>>> 1370639 ( Done 1.2)
    private String reason; // SPAM, OFFENSIVE, INAPPROPRIATE, OTHER

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

<<<<<<< HEAD
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
=======
    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
>>>>>>> 1370639 ( Done 1.2)
    private String status = "PENDING"; // PENDING, REVIEWED, RESOLVED, DISMISSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String resolution;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Report)) return false;
        Report report = (Report) o;
        return id != null && id.equals(report.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

