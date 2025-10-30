package com.edumoet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String location;

    private String website;

    private String githubUrl;

    private String linkedinUrl;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private Integer reputation = 1;

    @Column(nullable = false)
    private Integer views = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    // Track UPVOTES (Toggle: user vote hoặc undo)
    @ManyToMany
    @JoinTable(
        name = "user_votes_questions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> votedQuestions = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_votes_answers",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "answer_id")
    )
    private Set<Answer> votedAnswers = new HashSet<>();

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isBanned = false;

    private LocalDateTime bannedUntil;

    private String banReason;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    private String twoFactorSecret;

    @ManyToMany
    @JoinTable(
        name = "user_following",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    public void addPoints(Integer amount) {
        this.points += amount;
        updateLevel();
    }

    public void deductPoints(Integer amount) {
        this.points = Math.max(0, this.points - amount);
        updateLevel();
    }

    private void updateLevel() {
        // Simple level calculation: level = points / 100 + 1
        this.level = (this.points / 100) + 1;
    }

    public boolean isBanned() {
        if (!isBanned) return false;
        if (bannedUntil != null && LocalDateTime.now().isAfter(bannedUntil)) {
            isBanned = false;
            bannedUntil = null;
            banReason = null;
            return false;
        }
        return true;
    }

    public void follow(User user) {
        following.add(user);
    }

    public void unfollow(User user) {
        following.remove(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

